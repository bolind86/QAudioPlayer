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
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
    val currentPlaylistAudioFiles by viewModel.currentPlaylistAudioFiles.collectAsState()
    
    var currentScreen by remember { mutableStateOf("playlists") }
    
    Scaffold(
        topBar = {
            when (currentScreen) {
                "playlists" -> {
                    // 首页不显示TopBar，保持界面简洁
                }
                else -> {
                    // 其他界面的TopBar由各自界面处理
                }
            }
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
        when (currentScreen) {
            "playlists" -> {
                PlaylistManagerScreen(
                    playlists = playlists,
                    viewModel = viewModel,
                    onPlaylistSelected = { playlist ->
                        viewModel.selectPlaylist(playlist)
                        currentScreen = "playlist_detail"
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            "playlist_detail" -> {
                selectedPlaylist?.let { playlist ->
                    PlaylistDetailScreen(
                        playlist = playlist,
                        audioFiles = currentPlaylistAudioFiles,
                        currentAudioFile = currentAudioFile,
                        allAudioFiles = audioFiles,
                        viewModel = viewModel,
                        onBack = { currentScreen = "playlists" },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
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