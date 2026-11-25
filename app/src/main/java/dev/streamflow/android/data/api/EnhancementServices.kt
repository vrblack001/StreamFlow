package dev.streamflow.android.data.api

import dev.streamflow.android.data.model.DeArrowResponse
import dev.streamflow.android.data.model.RydResponse
import dev.streamflow.android.data.model.SponsorSegment
import retrofit2.http.*

/** SponsorBlock API — skip sponsor/intro/outro segments */
interface SponsorBlockService {
    @GET("api/skipSegments/{videoId}")
    suspend fun getSegments(
        @Path("videoId") videoId: String,
        @Query("categories") categories: String =
            "[\"sponsor\",\"intro\",\"outro\",\"interaction\",\"selfpromo\",\"music_offtopic\",\"preview\",\"filler\"]"
    ): List<SponsorSegment>
}

/** Return YouTube Dislike API */
interface ReturnYouTubeDislikeService {
    @GET("votes")
    suspend fun getVotes(@Query("videoId") videoId: String): RydResponse
}

/** DeArrow — community-sourced titles & thumbnails */
interface DeArrowService {
    @GET("api/branding")
    suspend fun getBranding(@Query("videoID") videoId: String): DeArrowResponse
}
