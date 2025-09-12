package com.audioplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.audioplayer.data.AudioFile
import com.audioplayer.data.PlayMode
import com.audioplayer.data.Playlist
import com.audioplayer.repository.AudioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import com.audioplayer.service.MediaController

class AudioPlayerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = AudioRepository(application)
    private var mediaController: MediaController? = null
    private var progressUpdateJob: Job? = null
    
    // 播放状态
    private val _currentAudioFile = MutableStateFlow<AudioFile?>(null)
    val currentAudioFile: StateFlow<AudioFile?> = _currentAudioFile.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _playMode = MutableStateFlow(PlayMode.SEQUENCE)
    val playMode: StateFlow<PlayMode> = _playMode.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    // 数据流
    val audioFiles: Flow<List<AudioFile>> = repository.getAllAudioFiles()
    val playlists: Flow<List<Playlist>> = repository.getAllPlaylists()
    
    private val _currentPlaylist = MutableStateFlow<List<AudioFile>>(emptyList())
    val currentPlaylist: StateFlow<List<AudioFile>> = _currentPlaylist.asStateFlow()
    
    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()
    
    private val _currentPlaylistAudioFiles = MutableStateFlow<List<AudioFile>>(emptyList())
    val currentPlaylistAudioFiles: StateFlow<List<AudioFile>> = _currentPlaylistAudioFiles.asStateFlow()
    
    private var currentIndex = 0
    
    fun setMediaController(controller: MediaController) {
        mediaController = controller
        // 设置播放完成回调
        controller.setOnCompletionCallback {
            handlePlaybackCompletion()
        }
    }
    
    private fun startProgressUpdate() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (_isPlaying.value) {
                mediaController?.let { controller ->
                    val currentPos = controller.getCurrentPosition().toLong()
                    val duration = controller.getDuration().toLong()
                    
                    _currentPosition.value = currentPos
                    if (duration > 0) {
                        _duration.value = duration
                    }
                }
                delay(500) // 每0.5秒更新一次，让进度条更流畅
            }
        }
    }
    
    private fun stopProgressUpdate() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }
    
    private fun handlePlaybackCompletion() {
        android.util.Log.d("AudioPlayerVM", "handlePlaybackCompletion called, playMode: ${_playMode.value}")
        
        // 更新UI状态
        _isPlaying.value = false
        stopProgressUpdate()
        
        // 根据播放模式处理下一首
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) {
            android.util.Log.d("AudioPlayerVM", "Playlist is empty, stopping")
            return
        }
        
        android.util.Log.d("AudioPlayerVM", "Current index: $currentIndex, playlist size: ${playlist.size}")
        
        when (_playMode.value) {
            PlayMode.SEQUENCE -> {
                // 顺序播放：播放下一首，如果是最后一首则停止
                if (currentIndex < playlist.size - 1) {
                    currentIndex++
                    android.util.Log.d("AudioPlayerVM", "SEQUENCE: Playing next track at index $currentIndex")
                    playAudio(playlist[currentIndex], playlist)
                } else {
                    // 最后一首播放完毕，停止播放
                    android.util.Log.d("AudioPlayerVM", "SEQUENCE: Reached end of playlist, stopping")
                    _currentPosition.value = 0L
                }
            }
            PlayMode.LOOP_ALL -> {
                // 列表循环：播放下一首，如果是最后一首则回到第一首
                currentIndex = (currentIndex + 1) % playlist.size
                android.util.Log.d("AudioPlayerVM", "LOOP_ALL: Playing track at index $currentIndex")
                playAudio(playlist[currentIndex], playlist)
            }
            PlayMode.LOOP_ONE -> {
                // 单曲循环：重新播放当前歌曲
                android.util.Log.d("AudioPlayerVM", "LOOP_ONE: Repeating current track at index $currentIndex")
                playAudio(playlist[currentIndex], playlist)
            }
            PlayMode.SHUFFLE -> {
                // 随机播放：随机选择下一首
                currentIndex = (0 until playlist.size).random()
                android.util.Log.d("AudioPlayerVM", "SHUFFLE: Playing random track at index $currentIndex")
                playAudio(playlist[currentIndex], playlist)
            }
        }
    }
    
    fun initializeAudioFiles() {
        viewModelScope.launch {
            try {
                val scannedFiles = repository.scanAudioFiles()
                repository.insertAudioFiles(scannedFiles)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun refreshAudioFiles() {
        viewModelScope.launch {
            try {
                val scannedFiles = repository.scanAudioFiles()
                repository.insertAudioFiles(scannedFiles)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun addAudioFileFromPath(filePath: String) {
        viewModelScope.launch {
            try {
                val file = java.io.File(filePath)
                if (file.exists() && file.isFile) {
                    val fileManager = com.audioplayer.utils.FileManager(getApplication())
                    val audioFiles = fileManager.scanAudioFilesInFolder(file.parent ?: "")
                    val targetFile = audioFiles.find { it.path == filePath }
                    targetFile?.let {
                        repository.insertAudioFile(it)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun playAudio(audioFile: AudioFile, playlist: List<AudioFile> = emptyList()) {
        _currentAudioFile.value = audioFile
        _duration.value = audioFile.duration
        
        if (playlist.isNotEmpty()) {
            _currentPlaylist.value = playlist
            currentIndex = playlist.indexOf(audioFile).takeIf { it >= 0 } ?: 0
        }
        
        // 调用AudioService播放音频
        mediaController?.play(audioFile)
        _isPlaying.value = true
        startProgressUpdate()
    }
    
    fun pauseAudio() {
        mediaController?.pause()
        _isPlaying.value = false
        stopProgressUpdate()
    }
    
    fun resumeAudio() {
        mediaController?.resume()
        _isPlaying.value = true
        startProgressUpdate()
    }
    
    fun stopAudio() {
        mediaController?.stop()
        _isPlaying.value = false
        _currentPosition.value = 0L
        stopProgressUpdate()
    }
    
    fun seekTo(position: Long) {
        mediaController?.seekTo(position.toInt())
        _currentPosition.value = position
        
        // 检查是否因为seek操作而开始播放，如果是则更新状态
        mediaController?.let { controller ->
            if (controller.isPlaying() && !_isPlaying.value) {
                _isPlaying.value = true
                startProgressUpdate()
            }
        }
    }
    
    fun nextTrack() {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return
        
        when (_playMode.value) {
            PlayMode.SEQUENCE -> {
                if (currentIndex < playlist.size - 1) {
                    currentIndex++
                    playAudio(playlist[currentIndex], playlist)
                } else {
                    stopAudio()
                }
            }
            PlayMode.LOOP_ALL -> {
                currentIndex = (currentIndex + 1) % playlist.size
                playAudio(playlist[currentIndex], playlist)
            }
            PlayMode.LOOP_ONE -> {
                playAudio(playlist[currentIndex], playlist)
            }
            PlayMode.SHUFFLE -> {
                currentIndex = (0 until playlist.size).random()
                playAudio(playlist[currentIndex], playlist)
            }
        }
    }
    
    fun previousTrack() {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return
        
        when (_playMode.value) {
            PlayMode.SEQUENCE, PlayMode.LOOP_ALL -> {
                if (currentIndex > 0) {
                    currentIndex--
                } else {
                    currentIndex = playlist.size - 1
                }
                playAudio(playlist[currentIndex], playlist)
            }
            PlayMode.LOOP_ONE -> {
                playAudio(playlist[currentIndex], playlist)
            }
            PlayMode.SHUFFLE -> {
                currentIndex = (0 until playlist.size).random()
                playAudio(playlist[currentIndex], playlist)
            }
        }
    }
    
    fun setPlayMode(mode: PlayMode) {
        _playMode.value = mode
    }
    
    fun updatePosition(position: Long) {
        _currentPosition.value = position
    }
    
    fun setPlaylist(playlist: List<AudioFile>) {
        _currentPlaylist.value = playlist
        currentIndex = 0
    }
    
    fun createPlaylist(name: String): Playlist {
        return Playlist(name = name)
    }
    
    fun savePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.insertPlaylist(playlist)
        }
    }
    
    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }
    
    fun createPlaylistFromFolder(name: String, folderPath: String) {
        viewModelScope.launch {
            try {
                val playlist = Playlist(name = name)
                repository.insertPlaylist(playlist)
                
                // 扫描文件夹中的音频文件
                val fileManager = com.audioplayer.utils.FileManager(getApplication())
                val audioFiles = fileManager.scanAudioFilesInFolder(folderPath)
                
                // 添加到数据库
                repository.insertAudioFiles(audioFiles)
                
                // 添加到播放列表
                audioFiles.forEachIndexed { index, audioFile ->
                    repository.addAudioFileToPlaylist(playlist.id, audioFile.id, index)
                }
                
            } catch (e: Exception) {
                android.util.Log.e("AudioPlayerVM", "Error creating playlist from folder: ${e.message}", e)
            }
        }
    }
    
    fun selectPlaylist(playlist: Playlist) {
        _selectedPlaylist.value = playlist
        viewModelScope.launch {
            repository.getPlaylistAudioFiles(playlist.id).collect { audioFiles ->
                _currentPlaylistAudioFiles.value = audioFiles
            }
        }
    }
    
    fun addAudioFileToCurrentPlaylist(audioFile: AudioFile) {
        val playlist = _selectedPlaylist.value ?: return
        viewModelScope.launch {
            val currentFiles = _currentPlaylistAudioFiles.value
            repository.addAudioFileToPlaylist(playlist.id, audioFile.id, currentFiles.size)
        }
    }
    
    fun removeAudioFileFromCurrentPlaylist(audioFile: AudioFile) {
        val playlist = _selectedPlaylist.value ?: return
        viewModelScope.launch {
            repository.removeAudioFileFromPlaylist(playlist.id, audioFile.id)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopProgressUpdate()
    }
}