package dev.streamflow.android.data.local

import androidx.room.*
import dev.streamflow.android.data.local.dao.*
import dev.streamflow.android.data.local.entity.*

@Database(
    entities = [
        WatchHistoryEntity::class,
        LocalSubscriptionEntity::class,
        LocalPlaylistEntity::class,
        LocalPlaylistVideoEntity::class,
        DownloadEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StreamFlowDatabase : RoomDatabase() {
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun playlistDao(): LocalPlaylistDao
    abstract fun downloadDao(): DownloadDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}
