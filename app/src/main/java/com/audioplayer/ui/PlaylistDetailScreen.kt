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
import com.audioplayer.data.Playlist
import com.audioplayer.viewmodel.AudioPlayerViewModel

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    audioFiles: List<AudioFile>,
    currentAudioFile: AudioFile?,
    allAudioFiles: List<AudioFile>,
    viewModel: AudioPlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddSongDialog by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        // 顶部栏
        TopAppBar(
            title = { 
                Text(
                    text = playlist.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { showAddSongDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加歌曲"
                    )
                }
            }
        )
        
        // 歌单信息
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${audioFiles.size} 首歌曲",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 播放全部按钮
                if (audioFiles.isNotEmpty()) {
                    Button(
                        onClick = {
                            viewModel.playAudio(audioFiles.first(), audioFiles)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("播放全部")
                    }
                }
            }
        }
        
        // 歌曲列表
        if (audioFiles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "歌单为空",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "点击 + 添加歌曲",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                items(audioFiles) { audioFile ->
                    PlaylistAudioFileItem(
                        audioFile = audioFile,
                        isCurrentPlaying = currentAudioFile?.id == audioFile.id,
                        onClick = {
                            viewModel.playAudio(audioFile, audioFiles)
                        },
                        onRemove = {
                            viewModel.removeAudioFileFromCurrentPlaylist(audioFile)
                        }
                    )
                }
            }
        }
    }
    
    // 添加歌曲对话框
    if (showAddSongDialog) {
        AddSongDialog(
            availableAudioFiles = allAudioFiles.filter { available ->
                audioFiles.none { it.id == available.id }
            },
            onDismiss = { showAddSongDialog = false },
            onSongSelected = { audioFile ->
                viewModel.addAudioFileToCurrentPlaylist(audioFile)
                showAddSongDialog = false
            }
        )
    }
}

@Composable
private fun PlaylistAudioFileItem(
    audioFile: AudioFile,
    isCurrentPlaying: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
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
            
            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "从歌单移除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    // 移除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("移除歌曲") },
            text = { Text("确定要从歌单中移除「${audioFile.title}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemove()
                        showDeleteDialog = false
                    }
                ) {
                    Text("移除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun AddSongDialog(
    availableAudioFiles: List<AudioFile>,
    onDismiss: () -> Unit,
    onSongSelected: (AudioFile) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加歌曲") },
        text = {
            if (availableAudioFiles.isEmpty()) {
                Text("没有可添加的歌曲")
            } else {
                LazyColumn(
                    modifier = Modifier.height(300.dp)
                ) {
                    items(availableAudioFiles) { audioFile ->
                        TextButton(
                            onClick = { onSongSelected(audioFile) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = audioFile.title,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (audioFile.artist != null) {
                                    Text(
                                        text = audioFile.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

private fun formatDuration(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}