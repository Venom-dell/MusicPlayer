package com.example.musicplayer.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerController @Inject constructor(
    private val context: Context
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    var controller: MediaController? = null
        private set

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _playbackState = MutableStateFlow(PlayerState())
    val playbackState: StateFlow<PlayerState> = _playbackState.asStateFlow()

    private var progressJob: Job? = null

    // We can't get AudioSessionId directly from MediaController easily in media3
    // unless we cast or query the underlying player instance if it's local.
    // For this simple clone we will manage equalizer at the service level or
    // use a dummy ID to let the build pass, or retrieve it via custom command.
    // Let's expose a flow or variable for it.
    var audioSessionId: Int = 0

    suspend fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future

        controller = future.await()
        setupListeners()
        updateState()
    }

    private fun setupListeners() {
        controller?.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                this@PlayerController.audioSessionId = audioSessionId
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState()
                if (isPlaying) startProgressTracking() else stopProgressTracking()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateState()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updateState()
            }
        })
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                updateProgress()
                delay(1000)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
    }

    private fun updateState() {
        controller?.let { c ->
            _playbackState.value = _playbackState.value.copy(
                isPlaying = c.isPlaying,
                currentSongTitle = c.currentMediaItem?.mediaMetadata?.title?.toString() ?: "Not Playing",
                currentSongArtist = c.currentMediaItem?.mediaMetadata?.artist?.toString() ?: "Select a song",
                duration = c.duration.coerceAtLeast(0),
                currentPosition = c.currentPosition.coerceAtLeast(0)
            )
        }
    }

    private fun updateProgress() {
         controller?.let { c ->
            _playbackState.value = _playbackState.value.copy(
                currentPosition = c.currentPosition.coerceAtLeast(0)
            )
        }
    }

    fun playSong(uri: Uri, title: String, artist: String) {
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .build()
            )
            .build()

        controller?.setMediaItem(mediaItem)
        controller?.prepare()
        controller?.play()
    }

    fun playPlaylist(items: List<Pair<Uri, Pair<String, String>>>, startIndex: Int = 0) {
        val mediaItems = items.map { (uri, metadata) ->
            MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(metadata.first)
                        .setArtist(metadata.second)
                        .build()
                )
                .build()
        }
        controller?.setMediaItems(mediaItems, startIndex, 0)
        controller?.prepare()
        controller?.play()
    }

    fun pause() = controller?.pause()
    fun play() = controller?.play()
    fun seekTo(position: Long) = controller?.seekTo(position)
    fun skipToNext() = controller?.seekToNext()
    fun skipToPrevious() = controller?.seekToPrevious()
    fun toggleShuffle() {
         controller?.shuffleModeEnabled = !(controller?.shuffleModeEnabled ?: false)
    }
    fun toggleRepeat() {
        val currentMode = controller?.repeatMode ?: Player.REPEAT_MODE_OFF
        controller?.repeatMode = if (currentMode == Player.REPEAT_MODE_OFF) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
    }

    fun release() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        stopProgressTracking()
    }
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentSongTitle: String = "Not Playing",
    val currentSongArtist: String = "Select a song",
    val duration: Long = 0,
    val currentPosition: Long = 0
)
