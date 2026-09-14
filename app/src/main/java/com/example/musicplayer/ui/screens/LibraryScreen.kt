package com.example.musicplayer.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.data.db.PlaylistEntity
import com.example.musicplayer.ui.viewmodels.LibraryViewModel
import com.example.musicplayer.ui.viewmodels.PlayerViewModel
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    playerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State is now hoisted to LibraryViewModel so it persists across tab navigation
    val selectedFolders by libraryViewModel.selectedFolders.collectAsState()
    val audioFiles by libraryViewModel.audioFiles.collectAsState()
    val playlists by libraryViewModel.playlists.collectAsState()
    val selectedPlaylist by libraryViewModel.selectedPlaylistWithSongs.collectAsState()

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)

            val documentFile = DocumentFile.fromTreeUri(context, it)
            documentFile?.listFiles()?.let { files ->
                val newAudioFiles = files.filter { file ->
                    file.type?.startsWith("audio/") == true
                }
                libraryViewModel.addFolder(it, newAudioFiles)
            }
        }
    }

    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Create Playlist") },
            text = {
                TextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        libraryViewModel.createPlaylist(newPlaylistName)
                    }
                    showCreatePlaylistDialog = false
                    newPlaylistName = ""
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Playlist Detail View
    if (selectedPlaylist != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { libraryViewModel.clearSelectedPlaylist() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    text = selectedPlaylist!!.playlist.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            val songs = selectedPlaylist!!.songs
            if (songs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("This playlist is empty.", color = Color.LightGray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(songs.size) { index ->
                        val song = songs[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val playlistQueue = songs.map {
                                        Pair(Uri.parse(it.uriString), Pair(it.title, it.artist))
                                    }
                                    playerViewModel.playPlaylist(playlistQueue, index)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.List, "Audio File", tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(song.title, color = Color.White, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                                Text(song.artist, color = Color.LightGray, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
        return // Return early to only show the playlist detail view
    }

    // Main Library View
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
            Row {
                 IconButton(onClick = { showCreatePlaylistDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Create Playlist",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { folderPickerLauncher.launch(null) }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Folder",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (playlists.isNotEmpty()) {
             Text("Playlists", color = Color.White, style = MaterialTheme.typography.titleMedium)
             LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(playlists) { playlist ->
                    Text(
                        text = playlist.name,
                        color = Color.LightGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { libraryViewModel.selectPlaylist(playlist.id) }
                            .padding(8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (audioFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No audio files added yet.\nClick + to add a folder.",
                    color = Color.LightGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            Text("All Songs", color = Color.White, style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(audioFiles) { file ->
                    AudioFileItem(
                        file = file,
                        playlists = playlists,
                        onAddToPlaylist = { playlistId ->
                             libraryViewModel.addSongToPlaylist(
                                 playlistId = playlistId,
                                 uriString = file.uri.toString(),
                                 title = file.name ?: "Unknown",
                                 artist = "Unknown Artist",
                                 album = "Unknown Album"
                             )
                        },
                        onEditMetadata = { title, artist, album ->
                            libraryViewModel.editMetadata(file.uri, title, artist, album) { success ->
                                if(success) {
                                   Toast.makeText(context, "Metadata updated", Toast.LENGTH_SHORT).show()
                                } else {
                                   Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onClick = {
                            playerViewModel.playSong(file.uri, file.name ?: "Unknown", "Unknown Artist")
                        },
                        onDelete = {
                            scope.launch {
                               try {
                                   if(file.delete()) {
                                       libraryViewModel.removeAudioFile(file.uri)
                                   }
                               } catch (e: Exception) {
                                   e.printStackTrace()
                               }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AudioFileItem(
    file: DocumentFile,
    playlists: List<PlaylistEntity>,
    onAddToPlaylist: (Long) -> Unit,
    onEditMetadata: (String, String, String) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showMetadataDialog by remember { mutableStateOf(false) }

    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            title = { Text("Add to Playlist") },
            text = {
                LazyColumn {
                    items(playlists) { playlist ->
                        Text(
                            text = playlist.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAddToPlaylist(playlist.id)
                                    showPlaylistDialog = false
                                }
                                .padding(16.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showMetadataDialog) {
        var editTitle by remember { mutableStateOf(file.name ?: "") }
        var editArtist by remember { mutableStateOf("") }
        var editAlbum by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showMetadataDialog = false },
            title = { Text("Edit Metadata") },
            text = {
                Column {
                    TextField(value = editTitle, onValueChange = { editTitle = it }, label = { Text("Title") })
                    Spacer(Modifier.height(8.dp))
                    TextField(value = editArtist, onValueChange = { editArtist = it }, label = { Text("Artist") })
                    Spacer(Modifier.height(8.dp))
                    TextField(value = editAlbum, onValueChange = { editAlbum = it }, label = { Text("Album") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onEditMetadata(editTitle, editArtist, editAlbum)
                    showMetadataDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMetadataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.List,
            contentDescription = "Audio File",
            tint = Color.LightGray,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name ?: "Unknown File",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
            Text(
                text = "Unknown Artist",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
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
                        showMetadataDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to Playlist") },
                    onClick = {
                        showMenu = false
                        showPlaylistDialog = true
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
