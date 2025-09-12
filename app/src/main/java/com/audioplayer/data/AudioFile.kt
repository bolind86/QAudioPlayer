package com.audioplayer.data

import androidx.room.*
import java.util.*

@Entity(tableName = "audio_files")
data class AudioFile(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val artist: String? = null,
    val album: String? = null,
    val duration: Long = 0L,
    val path: String,
    val folderPath: String,
    val size: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis()
)