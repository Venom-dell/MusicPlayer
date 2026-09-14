package com.example.musicplayer.ui.viewmodels

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.db.PlaylistDao
import com.example.musicplayer.data.db.PlaylistEntity
import com.example.musicplayer.data.db.PlaylistSongCrossRef
import com.example.musicplayer.data.db.PlaylistWithSongs
import com.example.musicplayer.data.db.SongEntity
import com.example.musicplayer.player.MetadataHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val metadataHelper: MetadataHelper
) : ViewModel() {

    // Hoisted state from LibraryScreen
    private val _selectedFolders = MutableStateFlow<List<Uri>>(emptyList())
    val selectedFolders: StateFlow<List<Uri>> = _selectedFolders.asStateFlow()

    private val _audioFiles = MutableStateFlow<List<DocumentFile>>(emptyList())
    val audioFiles: StateFlow<List<DocumentFile>> = _audioFiles.asStateFlow()

    val playlists = playlistDao.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPlaylistWithSongs = MutableStateFlow<PlaylistWithSongs?>(null)
    val selectedPlaylistWithSongs: StateFlow<PlaylistWithSongs?> = _selectedPlaylistWithSongs.asStateFlow()

    fun addFolder(uri: Uri, files: List<DocumentFile>) {
        _selectedFolders.value = _selectedFolders.value + uri
        _audioFiles.value = _audioFiles.value + files
    }

    fun removeAudioFile(uri: Uri) {
        _audioFiles.value = _audioFiles.value.filter { it.uri != uri }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistDao.insertPlaylist(PlaylistEntity(name = name))
        }
    }

    fun addSongToPlaylist(playlistId: Long, uriString: String, title: String, artist: String, album: String) {
        viewModelScope.launch {
            playlistDao.insertSong(SongEntity(uriString, title, artist, album))
            playlistDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(playlistId, uriString))
        }
    }

    fun editMetadata(uri: Uri, title: String, artist: String, album: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                metadataHelper.modifyMetadata(uri, title, artist, album)
            }
            onComplete(result)
        }
    }

    fun selectPlaylist(playlistId: Long) {
        viewModelScope.launch {
            val playlist = playlistDao.getPlaylistWithSongs(playlistId).firstOrNull()
            _selectedPlaylistWithSongs.value = playlist
        }
    }

    fun clearSelectedPlaylist() {
        _selectedPlaylistWithSongs.value = null
    }
}
