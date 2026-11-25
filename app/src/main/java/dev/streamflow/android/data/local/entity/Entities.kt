package dev.streamflow.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val thumbnail: String,
    val uploaderName: String,
    val duration: Long,
    val watchedAt: Long = System.currentTimeMillis(),
    val watchPositionMs: Long = 0L
)

@Entity(tableName = "subscriptions")
data class LocalSubscriptionEntity(
    @PrimaryKey val channelId: String,
    val name: String,
    val avatarUrl: String,
    val verified: Boolean = false,
    val subscribedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class LocalPlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val thumbnailUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_videos",
    primaryKeys = ["playlistId", "videoId"]
)
data class LocalPlaylistVideoEntity(
    val playlistId: Long,
    val videoId: String,
    val title: String,
    val thumbnail: String,
    val uploaderName: String,
    val duration: Long,
    val position: Int = 0
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val thumbnail: String,
    val filePath: String,
    val quality: String,
    val fileSize: Long = 0L,
    val downloadedAt: Long = System.currentTimeMillis(),
    val status: String = "completed"
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long = System.currentTimeMillis()
)
