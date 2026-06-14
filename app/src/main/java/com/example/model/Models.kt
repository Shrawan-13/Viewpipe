package com.example.model

data class VideoItem(
    val id: String,
    val title: String,
    val uploaderName: String,
    val uploaderUrl: String,
    val thumbnailUrl: String,
    val uploaderAvatarUrl: String = "",
    val viewCount: Long = 0,
    val durationText: String = "",
    val uploadDateText: String = ""
)

data class FeedResult(
    val items: List<VideoItem>,
    val nextPageUrl: String?
)
