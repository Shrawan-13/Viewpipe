package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_items ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryItem)

    @Query("DELETE FROM history_items WHERE id = :id")
    suspend fun deleteHistoryById(id: String)
    
    @Query("DELETE FROM history_items")
    suspend fun clearHistory()
}

@Dao
interface WatchLaterDao {
    @Query("SELECT * FROM watch_later_items ORDER BY timestamp DESC")
    fun getAllWatchLater(): Flow<List<WatchLaterItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchLater(item: WatchLaterItem)

    @Query("DELETE FROM watch_later_items WHERE id = :id")
    suspend fun deleteWatchLaterById(id: String)
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(sub: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE channelId = :id")
    suspend fun deleteSubscriptionById(id: String)
}
