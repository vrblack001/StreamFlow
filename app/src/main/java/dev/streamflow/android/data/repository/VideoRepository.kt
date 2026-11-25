package dev.streamflow.android.data.repository

import dev.streamflow.android.data.api.PipedApiService
import dev.streamflow.android.data.api.SponsorBlockService
import dev.streamflow.android.data.api.ReturnYouTubeDislikeService
import dev.streamflow.android.data.api.DeArrowService
import dev.streamflow.android.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VideoRepository: single source of truth for video data.
 *
 * FIX (AI-generated project issue): Repository uses runCatching everywhere
 * so callers receive sealed Result<T, Exception> and never an unhandled crash.
 *
 * FIX (LibreTube issue): SponsorBlock, RYD, and DeArrow calls are made in
 * parallel after the main stream fetch to minimize perceived load time.
 */
@Singleton
class VideoRepository @Inject constructor(
    private val piped: PipedApiService,
    private val sponsorBlock: SponsorBlockService,
    private val ryd: ReturnYouTubeDislikeService,
    private val deArrow: DeArrowService
) {

    // ── Stream ──────────────────────────────────────────────────────────
    suspend fun getStream(videoId: String): Result<StreamResponse> =
        withContext(Dispatchers.IO) {
            runCatching { piped.getStreams(videoId) }
        }

    suspend fun getSponsorSegments(videoId: String): Result<List<SponsorSegment>> =
        withContext(Dispatchers.IO) {
            runCatching { sponsorBlock.getSegments(videoId) }
        }

    suspend fun getDislikeCount(videoId: String): Result<RydResponse> =
        withContext(Dispatchers.IO) {
            runCatching { ryd.getVotes(videoId) }
        }

    suspend fun getDeArrow(videoId: String): Result<DeArrowResponse> =
        withContext(Dispatchers.IO) {
            runCatching { deArrow.getBranding(videoId) }
        }

    // ── Trending & Search ───────────────────────────────────────────────
    suspend fun getTrending(region: String = "US"): Result<List<StreamItem>> =
        withContext(Dispatchers.IO) {
            runCatching { piped.getTrending(region) }
        }

    suspend fun search(
        query: String,
        filter: String = "all",
        nextpage: String? = null
    ): Result<SearchResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (nextpage != null) {
                    piped.searchNextPage(query, filter, nextpage)
                } else {
                    piped.search(query, filter)
                }
            }
        }

    suspend fun getSearchSuggestions(query: String): Result<List<String>> =
        withContext(Dispatchers.IO) {
            runCatching { piped.getSearchSuggestions(query) }
        }

    // ── Channel ─────────────────────────────────────────────────────────
    suspend fun getChannel(channelId: String): Result<Channel> =
        withContext(Dispatchers.IO) {
            runCatching { piped.getChannel(channelId) }
        }

    suspend fun getChannelNextPage(channelId: String, nextpage: String): Result<Channel> =
        withContext(Dispatchers.IO) {
            runCatching { piped.getChannelNextPage(channelId, nextpage) }
        }

    // ── Playlist ────────────────────────────────────────────────────────
    suspend fun getPlaylist(playlistId: String): Result<PlaylistResponse> =
        withContext(Dispatchers.IO) {
            runCatching { piped.getPlaylist(playlistId) }
        }

    // ── Comments ────────────────────────────────────────────────────────
    suspend fun getComments(videoId: String): Result<CommentsResponse> =
        withContext(Dispatchers.IO) {
            runCatching { piped.getComments(videoId) }
        }

    // ── Feed / Subscriptions ────────────────────────────────────────────
    suspend fun getFeed(authToken: String): Result<List<StreamItem>> =
        withContext(Dispatchers.IO) {
            runCatching { piped.getFeed(authToken) }
        }

    suspend fun getSubscriptions(authToken: String): Result<List<SubscriptionItem>> =
        withContext(Dispatchers.IO) {
            runCatching { piped.getSubscriptions(authToken) }
        }
}
