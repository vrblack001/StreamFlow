package dev.streamflow.android.data.repository

import dev.streamflow.android.data.api.PipedApiService
import dev.streamflow.android.data.api.SponsorBlockService
import dev.streamflow.android.data.api.SubscriptionItem
import dev.streamflow.android.data.api.ReturnYouTubeDislikeService
import dev.streamflow.android.data.api.DeArrowService
import dev.streamflow.android.data.model.*
import dev.streamflow.android.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VideoRepository: Single source of truth.
 * Supports dual engines:
 * 1. Direct YouTube Extraction (via NewPipeExtractor - no API keys, directly from YouTube)
 * 2. Piped API (privacy proxy servers)
 */
@Singleton
class VideoRepository @Inject constructor(
    private val piped: PipedApiService,
    private val sponsorBlock: SponsorBlockService,
    private val ryd: ReturnYouTubeDislikeService,
    private val deArrow: DeArrowService,
    private val prefs: PreferencesManager
) {

    // ── Stream Extraction ───────────────────────────────────────────────
    suspend fun getStream(videoId: String): Result<StreamResponse> =
        withContext(Dispatchers.IO) {
            val useDirect = runCatching { prefs.useDirectYoutube.first() }.getOrDefault(true)
            if (useDirect) {
                // Try direct YouTube extraction first
                val directResult = fetchDirectYouTubeStream(videoId)
                if (directResult.isSuccess) {
                    return@withContext directResult
                }
            }

            // Fallback to Piped API
            runCatching { piped.getStreams(videoId) }.recoverCatching {
                // If Piped fails, try direct extraction as fallback
                fetchDirectYouTubeStream(videoId).getOrThrow()
            }
        }

    /**
     * Extracts YouTube streams directly without third-party proxy
     */
    private fun fetchDirectYouTubeStream(videoId: String): Result<StreamResponse> {
        return runCatching {
            val youtubeUrl = "https://www.youtube.com/watch?v=$videoId"
            val service = ServiceList.YouTube
            val streamInfo = StreamInfo.getInfo(service, youtubeUrl)

            val vStreams = streamInfo.videoStreams?.map { vs ->
                VideoStream(
                    url = vs.content ?: "",
                    quality = vs.resolution ?: "",
                    mimeType = vs.format?.mimeType ?: "",
                    codec = vs.codec ?: "",
                    videoOnly = vs.isVideoOnly,
                    width = vs.width,
                    height = vs.height,
                    fps = vs.fps,
                    bitrate = vs.averageBitrate
                )
            } ?: emptyList()

            val aStreams = streamInfo.audioStreams?.map { asStream ->
                AudioStream(
                    url = asStream.content ?: "",
                    format = asStream.format?.name ?: "",
                    quality = "${asStream.averageBitrate / 1000}kbps",
                    mimeType = asStream.format?.mimeType ?: "",
                    codec = asStream.codec ?: "",
                    bitrate = asStream.averageBitrate
                )
            } ?: emptyList()

            // Best playable URL: prefer video stream or audio stream
            val bestUrl = streamInfo.hlsUrl
                ?: vStreams.firstOrNull { !it.videoOnly }?.url
                ?: vStreams.firstOrNull()?.url
                ?: aStreams.firstOrNull()?.url
                ?: ""

            StreamResponse(
                title = streamInfo.name ?: "",
                description = streamInfo.description?.content ?: "",
                uploadDate = streamInfo.uploadDate?.offsetDateTime()?.toLocalDate()?.toString() ?: "",
                uploader = streamInfo.uploaderName ?: "",
                uploaderUrl = streamInfo.uploaderUrl ?: "",
                uploaderAvatar = streamInfo.uploaderAvatarUrl ?: "",
                uploaderVerified = streamInfo.isUploaderVerified,
                thumbnail = streamInfo.thumbnails?.maxByOrNull { it.width }?.url ?: "",
                duration = streamInfo.duration,
                views = streamInfo.viewCount,
                likes = streamInfo.likeCount,
                dislikes = 0L,
                hls = streamInfo.hlsUrl,
                dash = streamInfo.dashMpdUrl,
                videoStreams = vStreams,
                audioStreams = aStreams,
                relatedStreams = streamInfo.relatedItems?.filterIsInstance<org.schabi.newpipe.extractor.stream.StreamInfoItem>()?.map { rel ->
                    StreamItem(
                        url = rel.url ?: "",
                        title = rel.name ?: "",
                        thumbnail = rel.thumbnails?.maxByOrNull { it.width }?.url ?: "",
                        uploaderName = rel.uploaderName ?: "",
                        uploaderUrl = rel.uploaderUrl ?: "",
                        duration = rel.duration,
                        views = rel.viewCount,
                        isShort = rel.isShortFormContent
                    )
                } ?: emptyList()
            )
        }
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
    suspend fun getTrending(region: String = "SA"): Result<List<StreamItem>> =
        withContext(Dispatchers.IO) {
            runCatching { piped.getTrending(region) }.recoverCatching {
                // If Piped fails, fetch YouTube trending directly
                val kiosk = ServiceList.YouTube.kioskList.defaultKioskExtractor
                kiosk.fetchPage()
                kiosk.initialPage.items.filterIsInstance<org.schabi.newpipe.extractor.stream.StreamInfoItem>().map { item ->
                    StreamItem(
                        url = item.url ?: "",
                        title = item.name ?: "",
                        thumbnail = item.thumbnails?.maxByOrNull { it.width }?.url ?: "",
                        uploaderName = item.uploaderName ?: "",
                        uploaderUrl = item.uploaderUrl ?: "",
                        duration = item.duration,
                        views = item.viewCount
                    )
                }
            }
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
            }.recoverCatching {
                // Direct YouTube Search via NewPipeExtractor
                val searchExtractor = ServiceList.YouTube.getSearchExtractor(query)
                searchExtractor.fetchPage()
                val items = searchExtractor.initialPage.items.filterIsInstance<org.schabi.newpipe.extractor.stream.StreamInfoItem>().map { item ->
                    StreamItem(
                        url = item.url ?: "",
                        title = item.name ?: "",
                        thumbnail = item.thumbnails?.maxByOrNull { it.width }?.url ?: "",
                        uploaderName = item.uploaderName ?: "",
                        uploaderUrl = item.uploaderUrl ?: "",
                        duration = item.duration,
                        views = item.viewCount
                    )
                }
                SearchResult(items = items, nextpage = null)
            }
        }

    suspend fun getSearchSuggestions(query: String): Result<List<String>> =
        withContext(Dispatchers.IO) {
            runCatching { piped.getSearchSuggestions(query) }.recoverCatching {
                ServiceList.YouTube.suggestionExtractor.suggestionList(query)
            }
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
