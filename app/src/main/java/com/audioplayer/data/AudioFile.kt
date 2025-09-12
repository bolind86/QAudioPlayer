package com.audioplayer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_files")
data class AudioFile(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val filePath: String,
    val folderPath: String,
    val size: Long,
    val dateAdded: Long,
    val albumArt: String? = null
)