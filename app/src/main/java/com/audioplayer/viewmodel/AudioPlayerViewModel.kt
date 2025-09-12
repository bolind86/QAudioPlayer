package com.audioplayer.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.audioplayer.data.AudioFile
import com.audioplayer.data.PlayMode
import com.audioplayer.data.Playlist
import com.audioplayer.repository.AudioRepository
import com.audioplayer.service.MediaControllerManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AudioPlayerViewModel(
    private val repository: AudioRepository,
    private val context: Context
) : ViewModel() {
    
    private val mediaControllerManager = MediaControllerManager(context)
    
    private val _currentPlaylist = MutableStateFlow<List<AudioFile>>(emptyList())
    val currentPlaylist: StateFlow<List<AudioFile>> = _currentPlaylist.asStateFlow()
    
    private val _currentTrack = MutableStateFlow<AudioFile?>(null)
    val currentTrack: StateFlow<AudioFile?> = _currentTrack.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _playMode = MutableStateFlow(PlayMode.SEQUENTIAL)
    val playMode: StateFlow<PlayMode> = _playMode.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _volume = MutableStateFlow(0.8f)
    val volume: StateFlow<Float> = _volume.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    val playlists: StateFlow<List<Playlist>> = repository.getAllPlaylists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun addFolderToPlaylist(folderPath: String, playlistName: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                val playlistId = repository.addFolderToPlaylist(folderPath, playlistName)
                loadPlaylist(playlistId)
            } catch (e: Exception) {
                _errorMessage.value = "添加文件夹失败: ${e.message ?: "未知错误"}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    fun loadPlaylist(playlistId: String) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                repository.getPlaylistAudioFiles(playlistId)
                    .collect { audioFiles ->
                        _currentPlaylist.value = audioFiles
                        if (audioFiles.isNotEmpty()) {
                            val mediaItems = audioFiles.map { createMediaItem(it) }
                            mediaControllerManager.setPlaylist(mediaItems)
                            if (_currentTrack.value == null) {
                                _currentTrack.value = audioFiles.first()
                            }
                        }
                    }
            } catch (e: Exception) {
                _errorMessage.value = "加载播放列表失败: ${e.message ?: "未知错误"}"
            }
        }
    }
    
    init {
        mediaControllerManager.initialize()
        
        // 监听媒体控制器状态变化
        viewModelScope.launch {
            mediaControllerManager.isPlaying.collect { isPlaying ->
                _isPlaying.value = isPlaying
            }
        }
        
        viewModelScope.launch {
            mediaControllerManager.currentPosition.collect { position ->
                _currentPosition.value = position
            }
        }
        
        viewModelScope.launch {
            mediaControllerManager.duration.collect { duration ->
                _duration.value = duration
            }
        }
        
        viewModelScope.launch {
            mediaControllerManager.volume.collect { volume ->
                _volume.value = volume
            }
        }
    }
    
    fun playTrack(audioFile: AudioFile) {
        _currentTrack.value = audioFile
        val mediaItem = createMediaItem(audioFile)
        mediaControllerManager.setPlaylist(listOf(mediaItem))
        mediaControllerManager.play()
    }
    
    fun pauseTrack() {
        mediaControllerManager.pause()
    }
    
    fun resumeTrack() {
        mediaControllerManager.play()
    }
    
    private fun createMediaItem(audioFile: AudioFile): MediaItem {
        return MediaItem.Builder()
            .setUri(audioFile.filePath)
            .setMediaId(audioFile.id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(audioFile.title)
                    .setArtist(audioFile.artist)
                    .setAlbumTitle(audioFile.album)
                    .build()
            )
            .build()
    }
    
    fun playNext() {
        val playlist = _currentPlaylist.value
        val currentTrack = _currentTrack.value
        
        if (playlist.isEmpty() || currentTrack == null) return
        
        val currentIndex = playlist.indexOf(currentTrack)
        val nextIndex = when (_playMode.value) {
            PlayMode.SEQUENTIAL -> {
                if (currentIndex < playlist.size - 1) currentIndex + 1 else -1
            }
            PlayMode.REPEAT_ALL -> {
                if (currentIndex < playlist.size - 1) currentIndex + 1 else 0
            }
            PlayMode.REPEAT_ONE -> currentIndex
        }
        
        if (nextIndex >= 0) {
            _currentTrack.value = playlist[nextIndex]
            mediaControllerManager.skipToNext()
        } else {
            pauseTrack()
        }
    }
    
    fun playPrevious() {
        val playlist = _currentPlaylist.value
        val currentTrack = _currentTrack.value
        
        if (playlist.isEmpty() || currentTrack == null) return
        
        val currentIndex = playlist.indexOf(currentTrack)
        val previousIndex = when (_playMode.value) {
            PlayMode.SEQUENTIAL -> {
                if (currentIndex > 0) currentIndex - 1 else -1
            }
            PlayMode.REPEAT_ALL -> {
                if (currentIndex > 0) currentIndex - 1 else playlist.size - 1
            }
            PlayMode.REPEAT_ONE -> currentIndex
        }
        
        if (previousIndex >= 0) {
            _currentTrack.value = playlist[previousIndex]
            mediaControllerManager.skipToPrevious()
        }
    }
    
    fun setPlayMode(playMode: PlayMode) {
        _playMode.value = playMode
        val repeatMode = when (playMode) {
            PlayMode.SEQUENTIAL -> Player.REPEAT_MODE_OFF
            PlayMode.REPEAT_ALL -> Player.REPEAT_MODE_ALL
            PlayMode.REPEAT_ONE -> Player.REPEAT_MODE_ONE
        }
        mediaControllerManager.setRepeatMode(repeatMode)
    }
    
    fun seekTo(position: Long) {
        mediaControllerManager.seekTo(position)
    }
    
    fun setVolume(volume: Float) {
        mediaControllerManager.setVolume(volume)
    }
    
    fun updateDuration(duration: Long) {
        _duration.value = duration
    }
    
    fun updateCurrentPosition(position: Long) {
        _currentPosition.value = position
    }
    
    fun removePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.removePlaylist(playlist)
        }
    }
    
    fun refreshPlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.refreshPlaylist(playlistId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaControllerManager.release()
    }
}

class AudioPlayerViewModelFactory(
    private val repository: AudioRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioPlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioPlayerViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}