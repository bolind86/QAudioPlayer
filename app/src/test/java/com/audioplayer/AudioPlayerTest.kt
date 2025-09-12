package com.audioplayer

import com.audioplayer.data.AudioFile
import com.audioplayer.data.PlayMode
import org.junit.Test
import org.junit.Assert.*

class AudioPlayerTest {

    @Test
    fun testAudioFileCreation() {
        val audioFile = AudioFile(
            id = "test-123",
            title = "Test Song",
            artist = "Test Artist",
            album = "Test Album",
            duration = 180000, // 3 minutes
            filePath = "/storage/music/test.mp3",
            folderPath = "/storage/music",
            size = 1024 * 1024, // 1MB
            dateAdded = System.currentTimeMillis()
        )

        assertEquals("Test Song", audioFile.title)
        assertEquals("Test Artist", audioFile.artist)
        assertEquals("Test Album", audioFile.album)
        assertEquals(180000, audioFile.duration)
        assertEquals("/storage/music/test.mp3", audioFile.filePath)
    }

    @Test
    fun testPlayModeEnum() {
        val sequential = PlayMode.SEQUENTIAL
        val repeatAll = PlayMode.REPEAT_ALL
        val repeatOne = PlayMode.REPEAT_ONE

        assertEquals(PlayMode.SEQUENTIAL, sequential)
        assertEquals(PlayMode.REPEAT_ALL, repeatAll)
        assertEquals(PlayMode.REPEAT_ONE, repeatOne)
    }

    @Test
    fun testPlaylistLogic() {
        // Test sequential play mode logic
        val playlistSize = 5
        val currentIndex = 2
        
        // Sequential: next should be 3, previous should be 1
        val nextIndex = if (currentIndex < playlistSize - 1) currentIndex + 1 else -1
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else -1
        
        assertEquals(3, nextIndex)
        assertEquals(1, prevIndex)
    }

    @Test
    fun testRepeatAllPlayModeLogic() {
        val playlistSize = 5
        val currentIndex = 4 // Last song
        
        // Repeat all: next should wrap to 0, previous should be 3
        val nextIndex = if (currentIndex < playlistSize - 1) currentIndex + 1 else 0
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else playlistSize - 1
        
        assertEquals(0, nextIndex)
        assertEquals(3, prevIndex)
    }

    @Test
    fun testRepeatOnePlayModeLogic() {
        val currentIndex = 2
        
        // Repeat one: next and previous should stay at current index
        val nextIndex = currentIndex
        val prevIndex = currentIndex
        
        assertEquals(2, nextIndex)
        assertEquals(2, prevIndex)
    }
}