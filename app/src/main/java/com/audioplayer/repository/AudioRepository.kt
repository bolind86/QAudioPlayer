package com.audioplayer.repository

import com.audioplayer.data.*
import com.audioplayer.utils.FileManager
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AudioRepository(
    private val audioFileDao: AudioFileDao,
    private val playlistDao: PlaylistDao,
    private val fileManager: FileManager
) {
    
    fun getAllAudioFiles(): Flow<List<AudioFile>> = audioFileDao.getAllAudioFiles()
    
    fun getAudioFilesByFolder(folderPath: String): Flow<List<AudioFile>> = 
        audioFileDao.getAudioFilesByFolder(folderPath)
    
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    
    fun getPlaylistAudioFiles(playlistId: String): Flow<List<AudioFile>> = 
        playlistDao.getPlaylistAudioFiles(playlistId)
    
    suspend fun addFolderToPlaylist(folderPath: String, playlistName: String? = null): String {
        // 扫描文件夹中的音频文件
        val audioFiles = fileManager.scanAudioFilesInFolder(folderPath)
        
        // 保存音频文件到数据库
        audioFileDao.insertAudioFiles(audioFiles)
        
        // 创建播放列表
        val playlistId = UUID.randomUUID().toString()
        val playlist = Playlist(
            id = playlistId,
            name = playlistName ?: folderPath.substringAfterLast("/"),
            folderPath = folderPath,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        playlistDao.insertPlaylist(playlist)
        
        // 添加音频文件到播放列表
        audioFiles.forEachIndexed { index, audioFile ->
            val playlistAudioFile = PlaylistAudioFile(
                id = UUID.randomUUID().toString(),
                playlistId = playlistId,
                audioFileId = audioFile.id,
                position = index
            )
            playlistDao.insertPlaylistAudioFile(playlistAudioFile)
        }
        
        return playlistId
    }
    
    suspend fun removePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
        playlistDao.clearPlaylistAudioFiles(playlist.id)
    }
    
    suspend fun refreshPlaylist(playlistId: String) {
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return
        
        // 清除旧的文件关联
        playlistDao.clearPlaylistAudioFiles(playlistId)
        audioFileDao.deleteAudioFilesByFolder(playlist.folderPath)
        
        // 重新扫描文件夹
        val audioFiles = fileManager.scanAudioFilesInFolder(playlist.folderPath)
        audioFileDao.insertAudioFiles(audioFiles)
        
        // 重新添加到播放列表
        audioFiles.forEachIndexed { index, audioFile ->
            val playlistAudioFile = PlaylistAudioFile(
                id = UUID.randomUUID().toString(),
                playlistId = playlistId,
                audioFileId = audioFile.id,
                position = index
            )
            playlistDao.insertPlaylistAudioFile(playlistAudioFile)
        }
        
        // 更新播放列表时间戳
        val updatedPlaylist = playlist.copy(updatedAt = System.currentTimeMillis())
        playlistDao.insertPlaylist(updatedPlaylist)
    }
}