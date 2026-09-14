package com.example.musicplayer.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen() {
    val context = LocalContext.current
    var selectedFolders by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var audioFiles by remember { mutableStateOf<List<DocumentFile>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // Launcher for selecting a folder directory
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            // Persist permission to read this folder across reboots
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)

            selectedFolders = selectedFolders + it

            // Scan for audio files in this folder
            val documentFile = DocumentFile.fromTreeUri(context, it)
            documentFile?.listFiles()?.let { files ->
                val newAudioFiles = files.filter { file ->
                    file.type?.startsWith("audio/") == true
                }
                audioFiles = audioFiles + newAudioFiles
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Library",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            IconButton(onClick = { folderPickerLauncher.launch(null) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Folder",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (audioFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No audio files added yet.\nClick + to add a folder.",
                    color = Color.LightGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(audioFiles) { file ->
                    AudioFileItem(file = file, onDelete = {
                        scope.launch {
                           // Temporary simple delete
                           try {
                               file.delete()
                               audioFiles = audioFiles.filter { it.uri != file.uri }
                           } catch (e: Exception) {
                               e.printStackTrace()
                           }
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun AudioFileItem(file: DocumentFile, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Play song */ }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.List, // Placeholder for album art
            contentDescription = "Audio File",
            tint = Color.LightGray,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name ?: "Unknown File",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Unknown Artist",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit Metadata") },
                    onClick = {
                        showMenu = false
                        // TODO: Open edit metadata dialog
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to Playlist") },
                    onClick = {
                        showMenu = false
                        // TODO: Add to playlist dialog
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete from Device") },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}
