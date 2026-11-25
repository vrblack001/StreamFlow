package dev.streamflow.android.data.api

import dev.streamflow.android.data.model.*
import retrofit2.http.*

/**
 * Piped REST API service.
 * Base URL is set at runtime from user preferences (supports custom instances).
 * Default: https://pipedapi.kavin.rocks
 *
 * FIX (LibreTube issue): instance URL is NOT hardcoded — users can switch to
 * any healthy community Piped instance from Settings.
 */
interface PipedApiService {

    @GET("streams/{videoId}")
    suspend fun getStreams(@Path("videoId") videoId: String): StreamResponse

    @GET("trending")
    suspend fun getTrending(@Query("region") region: String = "US"): List<StreamItem>

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("filter") filter: String = "all",
        @Query("nextpage") nextpage: String? = null
    ): SearchResult

    @GET("nextpage/search")
    suspend fun searchNextPage(
        @Query("q") query: String,
        @Query("filter") filter: String = "all",
        @Query("nextpage") nextpage: String
    ): SearchResult

    @GET("channel/{channelId}")
    suspend fun getChannel(@Path("channelId") channelId: String): Channel

    @GET("nextpage/channel/{channelId}")
    suspend fun getChannelNextPage(
        @Path("channelId") channelId: String,
        @Query("nextpage") nextpage: String
    ): Channel

    @GET("playlists/{playlistId}")
    suspend fun getPlaylist(@Path("playlistId") playlistId: String): PlaylistResponse

    @GET("nextpage/playlists/{playlistId}")
    suspend fun getPlaylistNextPage(
        @Path("playlistId") playlistId: String,
        @Query("nextpage") nextpage: String
    ): PlaylistResponse

    @GET("comments/{videoId}")
    suspend fun getComments(@Path("videoId") videoId: String): CommentsResponse

    @GET("nextpage/comments/{videoId}")
    suspend fun getCommentsNextPage(
        @Path("videoId") videoId: String,
        @Query("nextpage") nextpage: String
    ): CommentsResponse

    @GET("suggestions")
    suspend fun getSearchSuggestions(@Query("query") query: String): List<String>

    // ── Piped authenticated endpoints ──
    @POST("login")
    suspend fun login(@Body body: Map<String, String>): Map<String, String>

    @GET("feed")
    suspend fun getFeed(@Query("authToken") token: String): List<StreamItem>

    @GET("subscriptions")
    suspend fun getSubscriptions(@Query("authToken") token: String): List<SubscriptionItem>

    @POST("subscribe")
    suspend fun subscribe(
        @Query("authToken") token: String,
        @Body body: Map<String, String>
    )

    @POST("unsubscribe")
    suspend fun unsubscribe(
        @Query("authToken") token: String,
        @Body body: Map<String, String>
    )
}

data class SubscriptionItem(
    val url: String = "",
    val name: String = "",
    val avatar: String = "",
    val verified: Boolean = false,
    val description: String? = null
)
