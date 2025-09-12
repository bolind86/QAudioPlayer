package com.audioplayer.service

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.audioplayer.MainActivity
import com.audioplayer.data.AudioFile
import android.net.Uri
import java.io.File

class AudioService : Service() {
    
    private val binder = AudioBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioFile: AudioFile? = null
    private var isPlayingState = false
    private var onCompletionCallback: (() -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "AudioPlayerChannel"
    }
    
    inner class AudioBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
    
    fun playAudio(audioFile: AudioFile) {
        try {
            currentAudioFile = audioFile
            
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                // 检查文件是否存在
                val file = File(audioFile.path)
                if (file.exists()) {
                    setDataSource(audioFile.path)
                } else {
                    // 如果文件不存在，尝试作为URI处理
                    setDataSource(this@AudioService, Uri.parse(audioFile.path))
                }
                
                setOnPreparedListener {
                    start()
                    isPlayingState = true
                    startForeground(NOTIFICATION_ID, createNotification())
                }
                setOnCompletionListener {
                    android.util.Log.d("AudioService", "onCompletion called for: ${audioFile.title}")
                    isPlayingState = false
                    // 确保在主线程中执行回调
                    mainHandler.post {
                        android.util.Log.d("AudioService", "Invoking completion callback")
                        onCompletionCallback?.invoke()
                    }
                }
                setOnErrorListener { _, what, extra ->
                    android.util.Log.e("AudioService", "MediaPlayer error: what=$what, extra=$extra")
                    android.util.Log.e("AudioService", "Audio file path: ${audioFile.path}")
                    isPlayingState = false
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            android.util.Log.e("AudioService", "Error playing audio: ${e.message}", e)
            isPlayingState = false
        }
    }
    
    fun pauseAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPlayingState = false
                startForeground(NOTIFICATION_ID, createNotification())
            }
        }
    }
    
    fun resumeAudio() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                isPlayingState = true
                startForeground(NOTIFICATION_ID, createNotification())
            }
        }
    }
    
    fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        isPlayingState = false
        currentAudioFile = null
        stopForeground(true)
    }
    
    fun isPlaying(): Boolean = isPlayingState
        
    fun getCurrentPosition(): Long = mediaPlayer?.currentPosition?.toLong() ?: 0L
        
    fun getDuration(): Long = mediaPlayer?.duration?.toLong() ?: 0L
        
    fun seekTo(position: Long) {
        mediaPlayer?.let { player ->
            player.seekTo(position.toInt())
            // 如果不在播放状态但MediaPlayer已准备好，则开始播放
            if (!isPlayingState && !player.isPlaying) {
                try {
                    player.start()
                    isPlayingState = true
                    startForeground(NOTIFICATION_ID, createNotification())
                } catch (e: Exception) {
                    android.util.Log.e("AudioService", "Error starting playback after seek: ${e.message}", e)
                }
            }
        }
    }
    
    fun getCurrentAudioFile(): AudioFile? = currentAudioFile
    
    fun setOnCompletionCallback(callback: () -> Unit) {
        android.util.Log.d("AudioService", "setOnCompletionCallback called")
        onCompletionCallback = callback
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Audio Player",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Audio Player Service"
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val title = currentAudioFile?.title ?: "Unknown"
        val artist = currentAudioFile?.artist ?: "Unknown Artist"
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
}