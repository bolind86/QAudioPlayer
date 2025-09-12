package com.audioplayer.service

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MediaControllerManager(private val context: Context) {
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var positionUpdateJob: Job? = null
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _volume = MutableStateFlow(0.8f)
    val volume: StateFlow<Float> = _volume.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    fun initialize() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, AudioService::class.java)
        )
        
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            _isConnected.value = true
            setupPlayerListener()
        }, androidx.core.content.ContextCompat.getMainExecutor(context))
    }
    
    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startPositionUpdater()
                } else {
                    stopPositionUpdater()
                }
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let {
                    _duration.value = mediaController?.duration ?: 0L
                    _currentPosition.value = 0L
                }
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _duration.value = mediaController?.duration ?: 0L
                        if (mediaController?.isPlaying == true) {
                            startPositionUpdater()
                        }
                    }
                    Player.STATE_IDLE, Player.STATE_ENDED -> {
                        stopPositionUpdater()
                    }
                }
            }
        })
    }
    
    private fun startPositionUpdater() {
        stopPositionUpdater()
        positionUpdateJob = scope.launch {
            while (isActive) {
                try {
                    mediaController?.let { controller ->
                        _currentPosition.value = controller.currentPosition
                        _duration.value = controller.duration
                    }
                    delay(100) // 更新频率：100ms
                } catch (e: Exception) {
                    // 处理异常
                    break
                }
            }
        }
    }
    
    private fun stopPositionUpdater() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
    
    fun play() {
        mediaController?.play()
    }
    
    fun pause() {
        mediaController?.pause()
    }
    
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        _currentPosition.value = position
    }
    
    fun skipToNext() {
        mediaController?.seekToNext()
    }
    
    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }
    
    fun setPlaylist(mediaItems: List<MediaItem>, startIndex: Int = 0) {
        mediaController?.setMediaItems(mediaItems, startIndex, 0)
        mediaController?.prepare()
    }
    
    fun setVolume(volume: Float) {
        mediaController?.volume = volume.coerceIn(0f, 1f)
        _volume.value = volume
    }
    
    fun setRepeatMode(repeatMode: Int) {
        mediaController?.repeatMode = repeatMode
    }
    
    fun release() {
        stopPositionUpdater()
        scope.cancel()
        mediaController?.release()
        controllerFuture?.cancel(true)
        _isConnected.value = false
    }
}