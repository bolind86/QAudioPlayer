package com.audioplayer.ui

import androidx.compose.foundation.clickable
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
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderPickerDialog(
    onDismiss: () -> Unit,
    onFolderSelected: (String) -> Unit,
    initialPath: String = "/storage/emulated/0"
) {
    var currentPath by remember { mutableStateOf(initialPath) }
    var folders by remember { mutableStateOf<List<File>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(currentPath) {
        isLoading = true
        folders = loadFolders(currentPath)
        isLoading = false
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 标题栏
                TopAppBar(
                    title = { 
                        Text(
                            text = "选择文件夹",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "关闭")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { onFolderSelected(currentPath) }
                        ) {
                            Text("选择当前文件夹")
                        }
                    }
                )
                
                // 当前路径显示
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentPath,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // 文件夹列表
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 返回上级目录
                        if (currentPath != "/") {
                            item {
                                FolderItem(
                                    folder = File(currentPath).parentFile ?: File("/"),
                                    isParentFolder = true,
                                    onClick = { 
                                        currentPath = it.absolutePath
                                    }
                                )
                            }
                        }
                        
                        // 子文件夹列表
                        items(folders) { folder ->
                            FolderItem(
                                folder = folder,
                                isParentFolder = false,
                                onClick = { 
                                    currentPath = it.absolutePath
                                },
                                onSelect = {
                                    onFolderSelected(it.absolutePath)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FolderItem(
    folder: File,
    isParentFolder: Boolean,
    onClick: (File) -> Unit,
    onSelect: ((File) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(folder) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isParentFolder) Icons.Default.ArrowBack else Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = if (isParentFolder) ".." else folder.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (!isParentFolder && onSelect != null) {
                IconButton(
                    onClick = { onSelect(folder) }
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "选择此文件夹",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun loadFolders(path: String): List<File> {
    return try {
        val directory = File(path)
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles { file ->
                file.isDirectory && !file.name.startsWith(".")
            }?.sortedBy { it.name.lowercase() } ?: emptyList()
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }
}