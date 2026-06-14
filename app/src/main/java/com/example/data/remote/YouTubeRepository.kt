package com.example.data.remote

import com.example.model.VideoItem
import com.example.model.FeedResult
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class YouTubeRepository {
    init {
        try {
            NewPipe.init(OkHttpDownloader())
        } catch(e: Throwable) {
            e.printStackTrace()
        }
    }

    fun getTrending(): FeedResult {
        return try {
            val service = ServiceList.YouTube
            val kio = service.kioskList.defaultKioskExtractor
            kio.fetchPage()
            
            val items = kio.initialPage.items.mapNotNull { item ->
                if (item is StreamInfoItem) {
                    VideoItem(
                        id = item.url,
                        title = item.name,
                        uploaderName = item.uploaderName,
                        uploaderUrl = item.uploaderUrl,
                        thumbnailUrl = item.thumbnails.firstOrNull()?.url ?: "",
                        viewCount = item.viewCount,
                        durationText = formatDuration(item.duration),
                        uploadDateText = item.uploadDate?.toString() ?: ""
                    )
                } else null
            }
            FeedResult(items, kio.initialPage.nextPage?.url)
        } catch (e: Throwable) {
            e.printStackTrace()
            FeedResult(emptyList(), null)
        }
    }

    fun search(query: String): FeedResult {
        return try {
            val service = ServiceList.YouTube
            val searchExtractor = service.getSearchExtractor(query)
            searchExtractor.fetchPage()
            
            val items = searchExtractor.initialPage.items.mapNotNull { item ->
                if (item is StreamInfoItem) {
                    VideoItem(
                        id = item.url,
                        title = item.name,
                        uploaderName = item.uploaderName,
                        uploaderUrl = item.url,
                        thumbnailUrl = item.thumbnails.firstOrNull()?.url ?: "",
                        viewCount = item.viewCount,
                        durationText = formatDuration(item.duration),
                        uploadDateText = item.uploadDate?.toString() ?: ""
                    )
                } else null
            }
            FeedResult(items, searchExtractor.initialPage.nextPage?.url)
        } catch (e: Throwable) {
            e.printStackTrace()
            FeedResult(emptyList(), null)
        }
    }

    private fun formatDuration(durationInSeconds: Long): String {
        val hours = durationInSeconds / 3600
        val minutes = (durationInSeconds % 3600) / 60
        val seconds = durationInSeconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    fun getStreamInfo(url: String): org.schabi.newpipe.extractor.stream.StreamInfo? {
        return try {
            org.schabi.newpipe.extractor.stream.StreamInfo.getInfo(ServiceList.YouTube, url)
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

}
