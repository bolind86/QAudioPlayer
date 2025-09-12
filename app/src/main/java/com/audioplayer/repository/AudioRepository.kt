package com.audioplayer.repository

import android.content.Context
import com.audioplayer.data.*
import com.audioplayer.utils.FileManager
import kotlinx.coroutines.flow.Flow

class AudioRepository(context: Context) {
    private val database = AudioDatabase.getDatabase(context)
    private val audioFileDao = database.audioFileDao()
    private val playlistDao = database.playlistDao()
    private val fileManager = FileManager(context)

    // 音频文件操作
    fun getAllAudioFiles(): Flow<List<AudioFile>> = audioFileDao.getAllAudioFiles()
    
    fun getAudioFilesByFolder(folderPath: String): Flow<List<AudioFile>> = 
        audioFileDao.getAudioFilesByFolder(folderPath)
    
    suspend fun insertAudioFile(audioFile: AudioFile) = audioFileDao.insertAudioFile(audioFile)
    
    suspend fun insertAudioFiles(audioFiles: List<AudioFile>) = audioFileDao.insertAudioFiles(audioFiles)
    
    suspend fun deleteAudioFile(audioFile: AudioFile) = audioFileDao.deleteAudioFile(audioFile)
    
    suspend fun scanAudioFiles(): List<AudioFile> = fileManager.scanAudioFiles()
    
    // 播放列表操作
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    
    suspend fun getPlaylistById(playlistId: String): Playlist? = playlistDao.getPlaylistById(playlistId)
    
    suspend fun insertPlaylist(playlist: Playlist) = playlistDao.insertPlaylist(playlist)
    
    suspend fun deletePlaylist(playlist: Playlist) = playlistDao.deletePlaylist(playlist)
    
    fun getPlaylistAudioFiles(playlistId: String): Flow<List<AudioFile>> = 
        playlistDao.getPlaylistAudioFiles(playlistId)
    
    suspend fun addAudioFileToPlaylist(playlistId: String, audioFileId: String, position: Int) {
        playlistDao.insertPlaylistAudioFile(PlaylistAudioFile(playlistId, audioFileId, position))
    }
    
    suspend fun removeAudioFileFromPlaylist(playlistId: String, audioFileId: String) {
        playlistDao.removeAudioFileFromPlaylist(playlistId, audioFileId)
    }
    
    suspend fun clearPlaylistAudioFiles(playlistId: String) = 
        playlistDao.clearPlaylistAudioFiles(playlistId)
}