package com.audioplayer.utils

import android.content.Context
import android.media.MediaMetadataRetriever
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
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: file.nameWithoutExtension
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            
            retriever.release()

            AudioFile(
                id = UUID.randomUUID().toString(),
                title = title,
                artist = artist,
                album = album,
                duration = duration,
                path = file.absolutePath,
                folderPath = file.parent ?: "",
                size = file.length(),
                dateAdded = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}
