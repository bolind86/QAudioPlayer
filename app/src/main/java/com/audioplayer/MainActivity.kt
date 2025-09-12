package com.audioplayer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.audioplayer.data.AudioDatabase
import com.audioplayer.repository.AudioRepository
import com.audioplayer.ui.AudioPlayerApp
import com.audioplayer.ui.theme.AudioPlayerTheme
import com.audioplayer.utils.FileManager
import com.audioplayer.viewmodel.AudioPlayerViewModel
import com.audioplayer.viewmodel.AudioPlayerViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // 处理权限结果
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 请求必要权限
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
        
        requestPermissionLauncher.launch(permissions)
        
        setContent {
            AudioPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AudioPlayerContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AudioPlayerContent() {
    val context = LocalContext.current
    
    // 权限检查
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    
    val permissionsState = rememberMultiplePermissionsState(permissions)
    
    // 初始化数据库和仓库
    val database = remember {
        Room.databaseBuilder(
            context,
            AudioDatabase::class.java,
            "audio_database"
        ).build()
    }
    
    val repository = remember {
        AudioRepository(
            audioFileDao = database.audioFileDao(),
            playlistDao = database.playlistDao(),
            fileManager = FileManager(context)
        )
    }
    
    val viewModel: AudioPlayerViewModel = viewModel(
        factory = AudioPlayerViewModelFactory(repository, context)
    )
    
    if (permissionsState.allPermissionsGranted) {
        AudioPlayerApp(viewModel = viewModel)
    } else {
        // 显示权限请求界面
        LaunchedEffect(Unit) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
}