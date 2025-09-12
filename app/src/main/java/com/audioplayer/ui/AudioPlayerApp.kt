package com.audioplayer.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.audioplayer.data.AudioFile
import com.audioplayer.data.PlayMode
import com.audioplayer.data.Playlist
import com.audioplayer.viewmodel.AudioPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerApp(viewModel: AudioPlayerViewModel) {
    val playlists by viewModel.playlists.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playMode by viewModel.playMode.collectAsState()
    val currentPlaylist by viewModel.currentPlaylist.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showFolderPicker by remember { mutableStateOf(false) }
    
    // 错误提示
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // 可以在这里显示 Snackbar 或其他错误提示
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部栏
        TopAppBar(
            title = { Text("音频播放器") },
            actions = {
                IconButton(
                    onClick = { showFolderPicker = true },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Add, contentDescription = "添加文件夹")
                    }
                }
            }
        )
        
        // 错误提示
        errorMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.clearErrorMessage() }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        // 播放列表
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(playlists) { playlist ->
                PlaylistItem(
                    playlist = playlist,
                    onPlaylistClick = { viewModel.loadPlaylist(playlist.id) },
                    onDeleteClick = { viewModel.removePlaylist(playlist) },
                    onRefreshClick = { viewModel.refreshPlaylist(playlist.id) }
                )
            }
            
            if (currentPlaylist.isNotEmpty()) {
                item {
                    Text(
                        text = "当前播放列表",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(currentPlaylist) { audioFile ->
                    AudioFileItem(
                        audioFile = audioFile,
                        isCurrentTrack = audioFile == currentTrack,
                        onTrackClick = { viewModel.playTrack(audioFile) }
                    )
                }
            }
        }
        
        // 底部播放控制器
        if (currentTrack != null) {
            EnhancedPlaybackControls(
                currentTrack = currentTrack!!,
                isPlaying = isPlaying,
                playMode = playMode,
                currentPosition = currentPosition,
                duration = duration,
                volume = volume,
                onPlayPause = {
                    if (isPlaying) {
                        viewModel.pauseTrack()
                    } else {
                        viewModel.resumeTrack()
                    }
                },
                onNext = { viewModel.playNext() },
                onPrevious = { viewModel.playPrevious() },
                onSeekTo = { viewModel.seekTo(it) },
                onVolumeChange = { viewModel.setVolume(it) },
                onPlayModeChange = { viewModel.setPlayMode(it) }
            )
        }
    }
    
    // 文件夹选择对话框
    if (showFolderPicker) {
        FolderPickerDialog(
            onDismiss = { showFolderPicker = false },
            onFolderSelected = { folderPath ->
                viewModel.addFolderToPlaylist(folderPath)
                showFolderPicker = false
            }
        )
    }
}

@Composable
fun PlaylistItem(
    playlist: Playlist,
    onPlaylistClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onPlaylistClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = playlist.folderPath,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row {
                IconButton(onClick = onRefreshClick) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新播放列表")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "删除播放列表")
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AudioFileItem(
    audioFile: AudioFile,
    isCurrentTrack: Boolean,
    onTrackClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isCurrentTrack) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isCurrentTrack) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        onClick = onTrackClick,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = audioFile.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${audioFile.artist} • ${audioFile.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            AnimatedVisibility(
                visible = isCurrentTrack,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "正在播放",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PlaybackControls(
    currentTrack: AudioFile,
    isPlaying: Boolean,
    playMode: PlayMode,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPlayModeChange: (PlayMode) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 当前歌曲信息
            Text(
                text = currentTrack.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = currentTrack.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 播放控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 播放模式按钮
                IconButton(
                    onClick = {
                        val nextMode = when (playMode) {
                            PlayMode.SEQUENTIAL -> PlayMode.REPEAT_ALL
                            PlayMode.REPEAT_ALL -> PlayMode.REPEAT_ONE
                            PlayMode.REPEAT_ONE -> PlayMode.SEQUENTIAL
                        }
                        onPlayModeChange(nextMode)
                    }
                ) {
                    val icon = when (playMode) {
                        PlayMode.SEQUENTIAL -> Icons.Default.PlayArrow
                        PlayMode.REPEAT_ALL -> Icons.Default.Repeat
                        PlayMode.REPEAT_ONE -> Icons.Default.RepeatOne
                    }
                    Icon(icon, contentDescription = "播放模式")
                }
                
                // 上一首
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "上一首")
                }
                
                // 播放/暂停
                IconButton(onClick = onPlayPause) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放"
                    )
                }
                
                // 下一首
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "下一首")
                }
                
                // 占位符，保持对称
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}