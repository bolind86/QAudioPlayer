package com.audioplayer

import com.audioplayer.data.PlayMode
import org.junit.Test
import org.junit.Assert.*

class PlayModeTest {
    
    @Test
    fun testPlayModeValues() {
        assertEquals(3, PlayMode.values().size)
        assertTrue(PlayMode.values().contains(PlayMode.SEQUENTIAL))
        assertTrue(PlayMode.values().contains(PlayMode.REPEAT_ALL))
        assertTrue(PlayMode.values().contains(PlayMode.REPEAT_ONE))
    }
    
    @Test
    fun testPlayModeToString() {
        assertEquals("SEQUENTIAL", PlayMode.SEQUENTIAL.toString())
        assertEquals("REPEAT_ALL", PlayMode.REPEAT_ALL.toString())
        assertEquals("REPEAT_ONE", PlayMode.REPEAT_ONE.toString())
    }
}