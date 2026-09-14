package com.example.musicplayer.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController
) : ViewModel() {

    val playerState = playerController.playbackState

    init {
        viewModelScope.launch {
            playerController.initializeController()
        }
    }

    fun playSong(uri: Uri, title: String, artist: String) {
        playerController.playSong(uri, title, artist)
    }

    fun playPlaylist(items: List<Pair<Uri, Pair<String, String>>>, startIndex: Int = 0) {
        playerController.playPlaylist(items, startIndex)
    }

    fun togglePlayPause() {
        if (playerState.value.isPlaying) {
            playerController.pause()
        } else {
            playerController.play()
        }
    }

    fun seekTo(position: Long) {
        playerController.seekTo(position)
    }

    fun skipToNext() {
        playerController.skipToNext()
    }

    fun skipToPrevious() {
        playerController.skipToPrevious()
    }

    fun toggleShuffle() {
        playerController.toggleShuffle()
    }

    fun toggleRepeat() {
        playerController.toggleRepeat()
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}
