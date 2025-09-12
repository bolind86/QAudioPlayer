package com.audioplayer.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.audioplayer.MainActivity
import com.audioplayer.data.PlayMode

class AudioService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private var playerNotificationManager: PlayerNotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        initializeSessionAndPlayer()
        setupNotification()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        playerNotificationManager?.setPlayer(null)
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun initializeSessionAndPlayer() {
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(AudioSessionCallback())
            .build()
    }

    private fun setupNotification() {
        playerNotificationManager = PlayerNotificationManager.Builder(this, NOTIFICATION_ID, CHANNEL_ID)
            .setMediaDescriptionAdapter(AudioNotificationAdapter(this))
            .setNotificationListener(AudioNotificationListener())
            .setChannelNameResourceId(androidx.core.R.string.status_bar_notification_info_overflow)
            .setChannelDescriptionResourceId(androidx.core.R.string.status_bar_notification_info_overflow)
            .build()
            
        playerNotificationManager?.apply {
            setUseFastForwardAction(true)
            setUseRewindAction(true)
            setUseNextAction(true)
            setUsePreviousAction(true)
            setUseStopAction(true)
            setPlayer(player)
        }
    }

    private inner class AudioSessionCallback : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): MutableList<MediaItem> {
            return mediaItems
        }
    }

    private inner class AudioNotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing) {
                startForeground(notificationId, notification)
            } else {
                stopForeground(false)
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            stopSelf()
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "audio_playback_channel"
    }
}

class PlaybackController(private val player: ExoPlayer) {
    private var currentPlayMode = PlayMode.SEQUENTIAL
    private var currentPlaylist: List<MediaItem> = emptyList()
    private var currentIndex = 0

    fun setPlayMode(playMode: PlayMode) {
        currentPlayMode = playMode
        updatePlayerRepeatMode()
    }

    fun setPlaylist(playlist: List<MediaItem>, startIndex: Int = 0) {
        currentPlaylist = playlist
        currentIndex = startIndex
        player.setMediaItems(playlist, startIndex, 0)
        player.prepare()
    }

    fun playNext(): Boolean {
        return when (currentPlayMode) {
            PlayMode.SEQUENTIAL -> {
                if (currentIndex < currentPlaylist.size - 1) {
                    currentIndex++
                    player.seekToNext()
                    true
                } else {
                    false
                }
            }
            PlayMode.REPEAT_ALL -> {
                currentIndex = if (currentIndex < currentPlaylist.size - 1) {
                    currentIndex + 1
                } else {
                    0
                }
                player.seekToNext()
                true
            }
            PlayMode.REPEAT_ONE -> {
                player.seekTo(0)
                true
            }
        }
    }

    fun playPrevious(): Boolean {
        return when (currentPlayMode) {
            PlayMode.SEQUENTIAL -> {
                if (currentIndex > 0) {
                    currentIndex--
                    player.seekToPrevious()
                    true
                } else {
                    false
                }
            }
            PlayMode.REPEAT_ALL -> {
                currentIndex = if (currentIndex > 0) {
                    currentIndex - 1
                } else {
                    currentPlaylist.size - 1
                }
                player.seekToPrevious()
                true
            }
            PlayMode.REPEAT_ONE -> {
                player.seekTo(0)
                true
            }
        }
    }

    private fun updatePlayerRepeatMode() {
        player.repeatMode = when (currentPlayMode) {
            PlayMode.SEQUENTIAL -> Player.REPEAT_MODE_OFF
            PlayMode.REPEAT_ALL -> Player.REPEAT_MODE_ALL
            PlayMode.REPEAT_ONE -> Player.REPEAT_MODE_ONE
        }
    }
}