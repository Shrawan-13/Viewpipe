package com.example.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocalRepository
import com.example.model.VideoItem
import com.example.ui.components.VideoCard
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(localRepository: LocalRepository, onVideoClick: (String) -> Unit) {
    val history by localRepository.history.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.History, contentDescription = "History")
            Spacer(modifier = Modifier.width(8.dp))
            Text("History", fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }

        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No watch history yet.")
            }
        } else {
            LazyColumn {
                items(history) { item ->
                    val video = VideoItem(
                        id = item.id,
                        title = item.title,
                        uploaderName = item.uploaderName,
                        thumbnailUrl = item.thumbnailUrl,
                        uploaderUrl = "",
                        viewCount = item.viewCount,
                        durationText = item.durationText,
                        uploadDateText = item.uploadDateText
                    )
                    VideoCard(video = video, onClick = { onVideoClick(item.id) })
                }
            }
        }
    }
}
