package com.audioplayer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey
    val id: String,
    val name: String,
    val folderPath: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "playlist_audio_files")
data class PlaylistAudioFile(
    @PrimaryKey
    val id: String,
    val playlistId: String,
    val audioFileId: String,
    val position: Int
)