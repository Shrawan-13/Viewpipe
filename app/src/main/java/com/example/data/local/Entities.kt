package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey val id: String,
    val title: String,
    val uploaderName: String,
    val thumbnailUrl: String,
    val viewCount: Long,
    val durationText: String,
    val uploadDateText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_later_items")
data class WatchLaterItem(
    @PrimaryKey val id: String,
    val title: String,
    val uploaderName: String,
    val thumbnailUrl: String,
    val viewCount: Long,
    val durationText: String,
    val uploadDateText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val channelId: String,
    val channelName: String,
    val channelAvatar: String
)
