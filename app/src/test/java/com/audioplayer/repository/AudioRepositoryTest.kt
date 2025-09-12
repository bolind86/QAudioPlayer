package com.audioplayer.repository

import com.audioplayer.data.AudioFile
import com.audioplayer.data.AudioFileDao
import com.audioplayer.data.Playlist
import com.audioplayer.data.PlaylistDao
import com.audioplayer.utils.FileManager
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class AudioRepositoryTest {

    @Test
    fun testAddFolderToPlaylist() = runBlocking {
        // Mock dependencies
        val audioFileDao = mock(AudioFileDao::class.java)
        val playlistDao = mock(PlaylistDao::class.java)
        val fileManager = mock(FileManager::class.java)
        
        // Create test audio files
        val testAudioFiles = listOf(
            AudioFile(
                id = "test-1",
                title = "Song 1",
                artist = "Artist 1",
                album = "Album 1",
                duration = 180000,
                filePath = "/music/song1.mp3",
                folderPath = "/music",
                size = 1024 * 1024,
                dateAdded = System.currentTimeMillis()
            ),
            AudioFile(
                id = "test-2",
                title = "Song 2",
                artist = "Artist 2",
                album = "Album 2",
                duration = 200000,
                filePath = "/music/song2.mp3",
                folderPath = "/music",
                size = 1024 * 1024,
                dateAdded = System.currentTimeMillis()
            )
        )
        
        // Setup mocks
        whenever(fileManager.scanAudioFilesInFolder("/music")).thenReturn(testAudioFiles)
        
        // Create repository
        val repository = AudioRepository(audioFileDao, playlistDao, fileManager)
        
        // Test adding folder to playlist
        val playlistId = repository.addFolderToPlaylist("/music", "Test Playlist")
        
        // Verify results
        assertNotNull(playlistId)
        assertTrue(playlistId.isNotEmpty())
        
        // Verify that DAO methods were called
        verify(audioFileDao).insertAudioFiles(testAudioFiles)
        verify(playlistDao).insertPlaylist(any())
        verify(playlistDao, times(2)).insertPlaylistAudioFile(any())
    }

    @Test
    fun testRemovePlaylist() = runBlocking {
        // Mock dependencies
        val audioFileDao = mock(AudioFileDao::class.java)
        val playlistDao = mock(PlaylistDao::class.java)
        val fileManager = mock(FileManager::class.java)
        
        val testPlaylist = Playlist(
            id = "test-playlist-123",
            name = "Test Playlist",
            folderPath = "/music",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val repository = AudioRepository(audioFileDao, playlistDao, fileManager)
        
        // Test removing playlist
        repository.removePlaylist(testPlaylist)
        
        // Verify that DAO methods were called
        verify(playlistDao).deletePlaylist(testPlaylist)
        verify(playlistDao).clearPlaylistAudioFiles(testPlaylist.id)
    }

    @Test
    fun testFormatTimeFunction() {
        // Test the formatTime function from PlaybackControls
        val time1 = 0L // 0 seconds
        val time2 = 65000L // 65 seconds = 1:05
        val time3 = 3661000L // 3661 seconds = 61:01
        
        assertEquals("0:00", formatTime(time1))
        assertEquals("1:05", formatTime(time2))
        assertEquals("61:01", formatTime(time3))
    }
    
    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}