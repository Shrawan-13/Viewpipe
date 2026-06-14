package com.example.ui.screens.watch

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.model.VideoItem
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Close
import com.example.ui.components.formatViewCount

import com.example.data.local.HistoryItem
import com.example.data.local.LocalRepository
import com.example.ViewPipeApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.stream.StreamInfo

import androidx.activity.compose.BackHandler

@OptIn(UnstableApi::class)
@Composable
fun WatchScreen(
    videoId: String,
    isMiniPlayer: Boolean = false,
    onNavigateUp: () -> Unit,
    onClose: () -> Unit = {},
    onMaximize: () -> Unit = {},
    localRepository: LocalRepository
) {
    BackHandler(enabled = !isMiniPlayer) {
        onNavigateUp()
    }

    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val application = context.applicationContext as ViewPipeApplication

    var streamInfo by remember { mutableStateOf<StreamInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var streamUrl by remember(streamInfo) { 
        mutableStateOf(streamInfo?.videoStreams?.firstOrNull()?.url ?: streamInfo?.videoOnlyStreams?.firstOrNull()?.url) 
    }

    LaunchedEffect(videoId) {
        withContext(Dispatchers.IO) {
            isLoading = true
            val info = application.youTubeRepository.getStreamInfo(videoId)
            withContext(Dispatchers.Main) {
                streamInfo = info
                isLoading = false
            }
            if (info != null) {
                localRepository.addHistory(
                    HistoryItem(
                        id = info.url,
                        title = info.name,
                        uploaderName = info.uploaderName,
                        thumbnailUrl = info.thumbnails.firstOrNull()?.url ?: "",
                        viewCount = info.viewCount,
                        durationText = formatDuration(info.duration),
                        uploadDateText = info.uploadDate?.toString() ?: ""
                    )
                )
            }
        }
    }

    var expandedQualityMenu by remember { mutableStateOf(false) }

    DisposableEffect(streamUrl) {
        val url = streamUrl
        val player = if (url != null) {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(Uri.parse(url))
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
        } else null
        
        exoPlayer = player

        onDispose {
            player?.release()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                exoPlayer?.pause()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                exoPlayer?.play()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (isMiniPlayer) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp) // adjust for bottom bar height
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onMaximize() }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(106.dp).fillMaxHeight().background(Color.Black)) {
                    if (exoPlayer != null) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    this.player = exoPlayer
                                    useController = false
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val thumbUrl = try { streamInfo?.thumbnails?.firstOrNull()?.url } catch (e: Exception) { null } ?: ""
                        if (thumbUrl.isNotEmpty()) {
                            AsyncImage(
                                model = thumbUrl,
                                contentDescription = "Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = streamInfo?.name ?: "Loading...", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = streamInfo?.uploaderName ?: "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
        // Player Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateUp) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = MaterialTheme.colorScheme.onBackground)
            }
            
            Box {
                IconButton(onClick = { expandedQualityMenu = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Quality", tint = MaterialTheme.colorScheme.onBackground)
                }
                DropdownMenu(
                    expanded = expandedQualityMenu,
                    onDismissRequest = { expandedQualityMenu = false }
                ) {
                    val streams = streamInfo?.videoStreams ?: streamInfo?.videoOnlyStreams ?: emptyList()
                    streams.distinctBy { it.resolution }.sortedByDescending { it.resolution.replace("p", "").toIntOrNull() ?: 0 }.forEach { stream ->
                        DropdownMenuItem(
                            text = { Text(stream.resolution) },
                            onClick = {
                                streamUrl = stream.url
                                expandedQualityMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Player Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
            } else if (exoPlayer != null) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = exoPlayer
                            useController = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "Failed to load video",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                }
            } else if (streamInfo != null) {
                val info = streamInfo!!
                // Title & Views
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = info.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${formatViewCount(info.viewCount)} • ${info.uploadDate?.toString() ?: ""}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Channel Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    ) {
                        val avatarUrl = try { info.uploaderAvatars.firstOrNull()?.url } catch (e: Exception) { null } ?: ""
                        if (avatarUrl.isNotEmpty()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = info.uploaderName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${formatViewCount(info.uploaderSubscriberCount)} subscribers",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { /*TODO*/ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Text("Subscribe", fontWeight = FontWeight.Bold)
                    }
                }

                // Actions Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionButton(Icons.Default.ThumbUp, formatViewCount(info.likeCount))
                    ActionButton(Icons.Default.Share, "Share")
                    ActionButton(Icons.Default.Download, "Download")
                    ActionButton(Icons.Outlined.LibraryAdd, "Save")
                }
                
                // Description box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                ) {
                    Text(
                        text = info.description?.content ?: "No description",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
}

@Composable
fun ActionButton(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp
        )
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
