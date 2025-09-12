@file:OptIn(ExperimentalMaterial3Api::class)

package com.audioplayer.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.audioplayer.data.Playlist
import com.audioplayer.viewmodel.AudioPlayerViewModel

@Composable
fun PlaylistManagerScreen(
    playlists: List<Playlist>,
    viewModel: AudioPlayerViewModel,
    onPlaylistSelected: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedFolderPath by remember { mutableStateOf<String?>(null) }
    
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val folderPath = getFolderPathFromUri(uri)
                if (folderPath != null) {
                    selectedFolderPath = folderPath
                    showCreateDialog = true
                }
            }
        }
    }
    
    Column(modifier = modifier.padding(16.dp)) {
        // 标题和操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "歌单管理",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Row {
                IconButton(
                    onClick = { showCreateDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "创建空歌单"
                    )
                }
                
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                        folderPickerLauncher.launch(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "从文件夹创建歌单"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 歌单列表
        if (playlists.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无歌单",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "点击 + 创建新歌单或从文件夹导入",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn {
                items(playlists) { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        onClick = { onPlaylistSelected(playlist) },
                        onDelete = { viewModel.deletePlaylist(playlist) }
                    )
                }
            }
        }
    }
    
    // 创建歌单对话框
    if (showCreateDialog) {
        CreatePlaylistDialog(
            isFromFolder = selectedFolderPath != null,
            onDismiss = { 
                showCreateDialog = false
                selectedFolderPath = null
            },
            onConfirm = { name ->
                if (selectedFolderPath != null) {
                    viewModel.createPlaylistFromFolder(name, selectedFolderPath!!)
                } else {
                    viewModel.savePlaylist(viewModel.createPlaylist(name))
                }
                showCreateDialog = false
                selectedFolderPath = null
            }
        )
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "创建时间: ${formatTimestamp(playlist.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除歌单",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除歌单") },
            text = { Text("确定要删除歌单「${playlist.name}」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun CreatePlaylistDialog(
    isFromFolder: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (isFromFolder) "从文件夹创建歌单" else "创建歌单") 
        },
        text = {
            Column {
                if (isFromFolder) {
                    Text(
                        text = "将从选择的文件夹中扫描所有音频文件并添加到歌单中",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                TextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("歌单名称") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (playlistName.isNotBlank()) {
                        onConfirm(playlistName.trim())
                    }
                },
                enabled = playlistName.isNotBlank()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun getFolderPathFromUri(uri: Uri): String? {
    return try {
        val docId = DocumentsContract.getTreeDocumentId(uri)
        docId
    } catch (e: Exception) {
        null
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(timestamp))
}