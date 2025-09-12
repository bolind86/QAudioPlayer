@file:OptIn(ExperimentalMaterial3Api::class)

package com.audioplayer.ui

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
fun AudioPlayerApp(
    viewModel: AudioPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val audioFiles by viewModel.audioFiles.collectAsState(initial = emptyList())
    val playlists by viewModel.playlists.collectAsState(initial = emptyList())
    val currentAudioFile by viewModel.currentAudioFile.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playMode by viewModel.playMode.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val currentPlaylist by viewModel.currentPlaylist.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AudioPlayer") },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshAudioFiles() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新音乐库"
                        )
                    }
                    IconButton(
                        onClick = { 
                            // TODO: 打开文件选择器
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加歌曲"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (currentAudioFile != null) {
                PlaybackControls(
                    currentAudioFile = currentAudioFile,
                    isPlaying = isPlaying,
                    playMode = playMode,
                    currentPosition = currentPosition,
                    duration = duration,
                    onPlayPause = {
                        if (isPlaying) {
                            viewModel.pauseAudio()
                        } else {
                            viewModel.resumeAudio()
                        }
                    },
                    onNext = { viewModel.nextTrack() },
                    onPrevious = { viewModel.previousTrack() },
                    onSeek = { position -> viewModel.seekTo(position) },
                    onPlayModeChange = { mode -> viewModel.setPlayMode(mode) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (audioFiles.isEmpty()) {
                // 显示文件夹选择器
                FolderPicker(
                    playlists = playlists,
                    onFolderSelected = { folderPath ->
                        // TODO: 实现文件夹选择逻辑
                    }
                )
            } else {
                // 显示音频文件列表
                Text(
                    text = "音乐库 (${audioFiles.size} 首歌曲)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn {
                    items(audioFiles) { audioFile ->
                        AudioFileItem(
                            audioFile = audioFile,
                            isCurrentPlaying = currentAudioFile?.id == audioFile.id,
                            onClick = {
                                viewModel.playAudio(audioFile, audioFiles)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioFileItem(
    audioFile: AudioFile,
    isCurrentPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlaying) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = if (isCurrentPlaying) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = audioFile.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCurrentPlaying) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                if (audioFile.artist != null) {
                    Text(
                        text = audioFile.artist,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = formatDuration(audioFile.duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
}

private fun formatDuration(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}