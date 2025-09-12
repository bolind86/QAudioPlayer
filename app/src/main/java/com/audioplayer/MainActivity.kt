package com.audioplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.audioplayer.service.MediaController
import com.audioplayer.ui.AudioPlayerApp
import com.audioplayer.ui.theme.AudioPlayerTheme
import com.audioplayer.viewmodel.AudioPlayerViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AudioPlayerViewModel by viewModels()
    private lateinit var mediaController: MediaController
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            viewModel.initializeAudioFiles()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化MediaController
        mediaController = MediaController(this)
        viewModel.setMediaController(mediaController)
        
        checkAndRequestPermissions()
        
        setContent {
            AudioPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AudioPlayerApp(viewModel = viewModel)
                }
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        mediaController.bindService()
    }
    
    override fun onStop() {
        super.onStop()
        mediaController.unbindService()
    }
    
    private fun checkAndRequestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            viewModel.initializeAudioFiles()
        }
    }
}