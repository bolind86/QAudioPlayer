package com.audioplayer.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.audioplayer.data.AudioFile
import kotlinx.coroutines.*

class MediaController(private val context: Context) {
    
    private var audioService: AudioService? = null
    private var bound = false
    private var pendingCompletionCallback: (() -> Unit)? = null
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as AudioService.AudioBinder
            audioService = binder.getService()
            bound = true
            
            // 如果有待设置的回调，现在设置它
            pendingCompletionCallback?.let {
                audioService?.setOnCompletionCallback(it)
                pendingCompletionCallback = null
            }
        }
        
        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
            audioService = null
        }
    }
    
    fun bindService() {
        val intent = Intent(context, AudioService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    fun unbindService() {
        if (bound) {
            context.unbindService(serviceConnection)
            bound = false
        }
    }
    
    fun play(audioFile: AudioFile) {
        audioService?.playAudio(audioFile)
    }
    
    fun pause() {
        audioService?.pauseAudio()
    }
    
    fun resume() {
        audioService?.resumeAudio()
    }
    
    fun stop() {
        audioService?.stopAudio()
    }
    
    fun seekTo(position: Int) {
        audioService?.seekTo(position.toLong())
    }
    
    fun getCurrentPosition(): Int = audioService?.getCurrentPosition()?.toInt() ?: 0
    
    fun getDuration(): Int = audioService?.getDuration()?.toInt() ?: 0
    
    fun isPlaying(): Boolean = audioService?.isPlaying() ?: false
    
    fun getCurrentAudioFile(): AudioFile? = audioService?.getCurrentAudioFile()
    
    fun setOnCompletionCallback(callback: () -> Unit) {
        if (bound && audioService != null) {
            // 服务已经绑定，直接设置回调
            audioService?.setOnCompletionCallback(callback)
        } else {
            // 服务还没有绑定，先保存回调
            pendingCompletionCallback = callback
        }
    }
}