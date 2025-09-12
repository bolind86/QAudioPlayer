package com.audioplayer.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.media3.common.Player
import androidx.media3.ui.PlayerNotificationManager
import com.audioplayer.MainActivity

class AudioNotificationAdapter(
    private val context: Context
) : PlayerNotificationManager.MediaDescriptionAdapter {

    override fun getCurrentContentTitle(player: Player): String {
        return player.currentMediaItem?.mediaMetadata?.title?.toString() ?: "Unknown Title"
    }

    override fun getCurrentContentText(player: Player): String? {
        val artist = player.currentMediaItem?.mediaMetadata?.artist?.toString()
        val album = player.currentMediaItem?.mediaMetadata?.albumTitle?.toString()
        
        return when {
            artist != null && album != null -> "$artist • $album"
            artist != null -> artist
            album != null -> album
            else -> "Unknown Artist"
        }
    }

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        // 返回默认的音乐图标，实际项目中可以从音频文件中提取专辑封面
        return BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_media_play)
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}