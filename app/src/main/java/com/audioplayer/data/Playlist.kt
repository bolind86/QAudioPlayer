package com.audioplayer.data

import androidx.room.*
import java.util.*

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_audio_files",
    primaryKeys = ["playlistId", "audioFileId"],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AudioFile::class,
            parentColumns = ["id"],
            childColumns = ["audioFileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlaylistAudioFile(
    val playlistId: String,
    val audioFileId: String,
    val position: Int
)