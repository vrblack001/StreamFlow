package dev.streamflow.android.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.streamflow.android.data.model.*
import dev.streamflow.android.data.repository.VideoRepository
import dev.streamflow.android.utils.PreferencesManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class PlayerUiState(
    val stream: StreamResponse? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val dislikes: Long? = null,
    val displayTitle: String? = null,
    val showSponsorSkipBanner: Boolean = false,
    val currentSponsorCategory: String = "",
    val isSubscribed: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val isLoadingComments: Boolean = false,
    val sponsorSegments: List<SponsorSegment> = emptyList()
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: VideoRepository,
    private val prefs: PreferencesManager,
    val player: ExoPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var sponsorCheckJob: Job? = null
    private var currentVideoId: String = ""

    // ── SponsorBlock polling ─────────────────────────────────────────────
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) startSponsorBlockPolling()
            else sponsorCheckJob?.cancel()
        }

        /**
         * FIX (AI common issue): Handle PlaybackException so the app never crashes.
         * Automatically retry with a refreshed stream URL on HTTP 403/410.
         */
        override fun onPlayerError(error: PlaybackException) {
            val cause = error.cause
            if (cause?.message?.contains("403") == true || cause?.message?.contains("410") == true) {
                // Stream URL expired — re-fetch and resume from current position
                val position = player.currentPosition
                viewModelScope.launch {
                    loadVideo(currentVideoId, resumePosition = position)
                }
            } else {
                _uiState.update {
                    it.copy(error = "Playback error: ${error.localizedMessage}")
                }
            }
        }
    }

    init {
        player.addListener(playerListener)
    }

    fun loadVideo(videoId: String, resumePosition: Long = 0L) {
        currentVideoId = videoId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Main stream fetch
            repository.getStream(videoId)
                .onSuccess { stream ->
                    _uiState.update { it.copy(stream = stream, isLoading = false) }
                    playStream(stream, resumePosition)

                    // Parallel enhancement fetches
                    launch { fetchSponsorSegments(videoId) }
                    launch { fetchDislikes(videoId) }
                    launch { fetchDeArrow(videoId) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = buildError(e))
                    }
                }
        }
    }

    private fun playStream(stream: StreamResponse, resumePosition: Long) {
        // Prefer HLS manifest if available (most compatible)
        val url = stream.hls
            ?: stream.dash
            ?: stream.videoStreams.firstOrNull()?.url
            ?: return

        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        if (resumePosition > 0) player.seekTo(resumePosition)
        player.play()
    }

    private suspend fun fetchSponsorSegments(videoId: String) {
        val enabled = prefs.sponsorBlockEnabled.first()
        if (!enabled) return
        repository.getSponsorSegments(videoId)
            .onSuccess { segments ->
                _uiState.update { it.copy(sponsorSegments = segments) }
            }
        // Silently ignore SponsorBlock failures — non-critical
    }

    private suspend fun fetchDislikes(videoId: String) {
        val enabled = prefs.rydEnabled.first()
        if (!enabled) return
        repository.getDislikeCount(videoId)
            .onSuccess { ryd ->
                _uiState.update { it.copy(dislikes = ryd.dislikes) }
            }
    }

    private suspend fun fetchDeArrow(videoId: String) {
        val enabled = prefs.deArrowEnabled.first()
        if (!enabled) return
        repository.getDeArrow(videoId)
            .onSuccess { dearrow ->
                val bestTitle = dearrow.titles
                    .filter { !it.original && it.votes >= 0 }
                    .maxByOrNull { it.votes }?.title
                _uiState.update { it.copy(displayTitle = bestTitle) }
            }
    }

    private fun startSponsorBlockPolling() {
        sponsorCheckJob?.cancel()
        sponsorCheckJob = viewModelScope.launch {
            while (isActive) {
                val currentMs = player.currentPosition
                val segments = _uiState.value.sponsorSegments
                val active = segments.firstOrNull { seg ->
                    currentMs >= seg.startMs && currentMs < seg.endMs
                }
                if (active != null) {
                    _uiState.update {
                        it.copy(
                            showSponsorSkipBanner = true,
                            currentSponsorCategory = active.category
                        )
                    }
                } else {
                    _uiState.update { it.copy(showSponsorSkipBanner = false) }
                }
                delay(500) // poll every 500ms
            }
        }
    }

    fun skipCurrentSegment() {
        val currentMs = player.currentPosition
        val segment = _uiState.value.sponsorSegments.firstOrNull { seg ->
            currentMs >= seg.startMs && currentMs < seg.endMs
        }
        segment?.let { player.seekTo(it.endMs) }
        _uiState.update { it.copy(showSponsorSkipBanner = false) }
    }

    fun seekToChapter(startSec: Long) {
        player.seekTo(startSec * 1000)
    }

    fun loadComments(videoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingComments = true) }
            repository.getComments(videoId)
                .onSuccess { result ->
                    _uiState.update { it.copy(comments = result.comments, isLoadingComments = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingComments = false) }
                }
        }
    }

    fun toggleSubscription() {
        // Local subscription toggle — extend with Piped auth if logged in
        _uiState.update { it.copy(isSubscribed = !it.isSubscribed) }
    }

    fun shareVideo(context: Context, videoId: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://youtu.be/$videoId")
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    fun like() { /* Piped authenticated like */ }
    fun dislike() { /* handled by RYD */ }
    fun showDownloadDialog() { /* trigger download dialog */ }
    fun showPlaylistDialog() { /* trigger save-to-playlist dialog */ }

    private fun buildError(e: Throwable): String = when {
        e.message?.contains("ReCaptcha") == true ->
            "YouTube is rate-limiting. Try switching the Piped instance in Settings → Network."
        e.message?.contains("429") == true ->
            "Too many requests. Try again in a moment."
        e.message?.contains("timeout") == true ->
            "Connection timed out. Check your internet."
        else -> "Failed to load video: ${e.localizedMessage}"
    }

    override fun onCleared() {
        player.removeListener(playerListener)
        player.release()
        super.onCleared()
    }
}
