package com.audioplayer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioFileDao {
    @Query("SELECT * FROM audio_files ORDER BY title ASC")
    fun getAllAudioFiles(): Flow<List<AudioFile>>
    
    @Query("SELECT * FROM audio_files WHERE folderPath = :folderPath ORDER BY title ASC")
    fun getAudioFilesByFolder(folderPath: String): Flow<List<AudioFile>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioFile(audioFile: AudioFile)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioFiles(audioFiles: List<AudioFile>)
    
    @Delete
    suspend fun deleteAudioFile(audioFile: AudioFile)
    
    @Query("DELETE FROM audio_files WHERE folderPath = :folderPath")
    suspend fun deleteAudioFilesByFolder(folderPath: String)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>
    
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: String): Playlist?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)
    
    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
    
    @Query("""
        SELECT af.* FROM audio_files af
        INNER JOIN playlist_audio_files paf ON af.id = paf.audioFileId
        WHERE paf.playlistId = :playlistId
        ORDER BY paf.position ASC
    """)
    fun getPlaylistAudioFiles(playlistId: String): Flow<List<AudioFile>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistAudioFile(playlistAudioFile: PlaylistAudioFile)
    
    @Query("DELETE FROM playlist_audio_files WHERE playlistId = :playlistId AND audioFileId = :audioFileId")
    suspend fun removeAudioFileFromPlaylist(playlistId: String, audioFileId: String)
    
    @Query("DELETE FROM playlist_audio_files WHERE playlistId = :playlistId")
    suspend fun clearPlaylistAudioFiles(playlistId: String)
}

@Database(
    entities = [AudioFile::class, Playlist::class, PlaylistAudioFile::class],
    version = 1,
    exportSchema = false
)
abstract class AudioDatabase : RoomDatabase() {
    abstract fun audioFileDao(): AudioFileDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: AudioDatabase? = null

        fun getDatabase(context: android.content.Context): AudioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AudioDatabase::class.java,
                    "audio_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}