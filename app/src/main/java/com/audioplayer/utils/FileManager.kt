package com.audioplayer.utils

import android.content.Context
import android.provider.MediaStore
import com.audioplayer.data.AudioFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class FileManager(private val context: Context) {

    suspend fun scanAudioFiles(): List<AudioFile> = withContext(Dispatchers.IO) {
        val audioFiles = mutableListOf<AudioFile>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED
        )
        
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            MediaStore.Audio.Media.IS_MUSIC + "=1",
            null,
            MediaStore.Audio.Media.TITLE + " ASC"
        )
        
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            
            while (it.moveToNext()) {
                val id = it.getLong(idColumn).toString()
                val title = it.getString(titleColumn) ?: "Unknown"
                val artist = it.getString(artistColumn)
                val album = it.getString(albumColumn)
                val duration = it.getLong(durationColumn)
                val path = it.getString(dataColumn) ?: continue
                val size = it.getLong(sizeColumn)
                val dateAdded = it.getLong(dateAddedColumn) * 1000
                
                val file = File(path)
                if (file.exists()) {
                    audioFiles.add(
                        AudioFile(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            path = path,
                            folderPath = file.parent ?: "",
                            size = size,
                            dateAdded = dateAdded
                        )
                    )
                }
            }
        }
        
        audioFiles
    }

    suspend fun scanAudioFilesInFolder(folderPath: String): List<AudioFile> =
        withContext(Dispatchers.IO) {
            val audioFiles = mutableListOf<AudioFile>()
            val folder = File(folderPath)
            
            if (!folder.exists() || !folder.isDirectory) {
                return@withContext emptyList()
            }

            scanFolderRecursively(folder, audioFiles)
            audioFiles
        }

    private fun scanFolderRecursively(folder: File, audioFiles: MutableList<AudioFile>) {
        folder.listFiles()?.forEach { file ->
            when {
                file.isDirectory -> {
                    scanFolderRecursively(file, audioFiles)
                }
                file.isAudioFile() -> {
                    val audioFile = createAudioFileFromFile(file)
                    audioFile?.let { audioFiles.add(it) }
                }
            }
        }
    }

    private fun File.isAudioFile(): Boolean {
        val audioExtensions = setOf("mp3", "flac", "aac", "ogg", "wav", "m4a", "wma")
        return audioExtensions.contains(extension.lowercase())
    }

    private fun createAudioFileFromFile(file: File): AudioFile? {
        return try {
            val mediaMetadata = getMediaMetadata(file.absolutePath)
            
            AudioFile(
                id = UUID.randomUUID().toString(),
                title = mediaMetadata.title ?: file.nameWithoutExtension,
                artist = mediaMetadata.artist,
                album = mediaMetadata.album,
                duration = mediaMetadata.duration,
                path = file.absolutePath,
                folderPath = file.parent ?: "",
                size = file.length(),
                dateAdded = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getMediaMetadata(filePath: String): MediaMetadata {
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )

        val selection = "${MediaStore.Audio.Media.DATA} = ?"
        val selectionArgs = arrayOf(filePath)

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)

                return MediaMetadata(
                    title = if (titleIndex >= 0) cursor.getString(titleIndex) else null,
                    artist = if (artistIndex >= 0) cursor.getString(artistIndex) else null,
                    album = if (albumIndex >= 0) cursor.getString(albumIndex) else null,
                    duration = if (durationIndex >= 0) cursor.getLong(durationIndex) else 0L,
                    albumArt = null
                )
            }
        }

        return MediaMetadata()
    }

    data class MediaMetadata(
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val duration: Long = 0L,
        val albumArt: String? = null
    )
}