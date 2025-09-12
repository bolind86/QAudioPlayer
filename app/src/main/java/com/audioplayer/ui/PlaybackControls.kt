package com.audioplayer.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.audioplayer.data.AudioFile
import com.audioplayer.data.PlayMode
import kotlin.math.roundToInt

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EnhancedPlaybackControls(
    currentTrack: AudioFile,
    isPlaying: Boolean,
    playMode: PlayMode,
    currentPosition: Long,
    duration: Long,
    volume: Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onPlayModeChange: (PlayMode) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(0f) }
    
    val playPauseScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "playPauseScale"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPlaying) 12.dp else 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 当前歌曲信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 专辑封面占位符
                Card(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentTrack.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${currentTrack.artist} • ${currentTrack.album}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // 音量控制
                VolumeControl(
                    volume = volume,
                    onVolumeChange = onVolumeChange
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 进度条
            Column {
                Slider(
                    value = if (isDragging) dragPosition else if (duration > 0) currentPosition.toFloat() / duration else 0f,
                    onValueChange = { value ->
                        isDragging = true
                        dragPosition = value
                    },
                    onValueChangeFinished = {
                        isDragging = false
                        val seekPosition = (dragPosition * duration).toLong()
                        onSeekTo(seekPosition)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                // 时间显示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(if (isDragging) (dragPosition * duration).toLong() else currentPosition),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTime(duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
                    val (icon, description) = when (playMode) {
                        PlayMode.SEQUENTIAL -> Icons.Default.PlayArrow to "顺序播放"
                        PlayMode.REPEAT_ALL -> Icons.Default.Repeat to "列表循环"
                        PlayMode.REPEAT_ONE -> Icons.Default.RepeatOne to "单曲循环"
                    }
                    Icon(
                        icon, 
                        contentDescription = description,
                        tint = if (playMode == PlayMode.SEQUENTIAL) 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                }
                
                // 上一首
                IconButton(onClick = onPrevious) {
                    Icon(
                        Icons.Default.SkipPrevious, 
                        contentDescription = "上一首",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // 播放/暂停 (大按钮)
                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(64.dp)
                        .scale(playPauseScale)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // 下一首
                IconButton(onClick = onNext) {
                    Icon(
                        Icons.Default.SkipNext, 
                        contentDescription = "下一首",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // 更多选项占位符
                IconButton(onClick = { /* TODO: 实现更多选项 */ }) {
                    Icon(
                        Icons.Default.MoreVert, 
                        contentDescription = "更多选项"
                    )
                }
            }
        }
    }
}

@Composable
fun VolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                when {
                    volume == 0f -> Icons.Default.VolumeOff
                    volume < 0.5f -> Icons.Default.VolumeDown
                    else -> Icons.Default.VolumeUp
                },
                contentDescription = "音量控制"
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Column(
                modifier = Modifier
                    .width(200.dp)
                    .padding(16.dp)
            ) {
                Text(
                    text = "音量: ${(volume * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}