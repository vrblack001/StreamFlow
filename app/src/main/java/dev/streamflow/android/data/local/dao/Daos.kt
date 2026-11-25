package dev.streamflow.android.data.local.dao

import androidx.room.*
import dev.streamflow.android.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE videoId = :videoId")
    suspend fun deleteById(videoId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY subscribedAt DESC")
    fun getAllSubscriptions(): Flow<List<LocalSubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: LocalSubscriptionEntity)

    @Delete
    suspend fun delete(subscription: LocalSubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE channelId = :channelId")
    suspend fun deleteByChannelId(channelId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM subscriptions WHERE channelId = :channelId)")
    fun isSubscribed(channelId: String): Flow<Boolean>
}

@Dao
interface LocalPlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<LocalPlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPlaylist(playlist: LocalPlaylistEntity): Long

    @Delete
    suspend fun deletePlaylist(playlist: LocalPlaylistEntity)

    @Query("SELECT * FROM playlist_videos WHERE playlistId = :playlistId ORDER BY position ASC")
    fun getVideosForPlaylist(playlistId: Long): Flow<List<LocalPlaylistVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addVideoToPlaylist(item: LocalPlaylistVideoEntity)

    @Query("DELETE FROM playlist_videos WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removeVideoFromPlaylist(playlistId: Long, videoId: String)
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: DownloadEntity)

    @Query("UPDATE downloads SET status = :status WHERE videoId = :videoId")
    suspend fun updateStatus(videoId: String, status: String)

    @Query("DELETE FROM downloads WHERE videoId = :videoId")
    suspend fun delete(videoId: String)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT query FROM search_history ORDER BY searchedAt DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun delete(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}
