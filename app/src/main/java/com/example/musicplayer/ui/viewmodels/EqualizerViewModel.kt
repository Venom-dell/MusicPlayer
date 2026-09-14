package com.example.musicplayer.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.musicplayer.player.EqualizerHelper
import com.example.musicplayer.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val equalizerHelper: EqualizerHelper,
    private val playerController: PlayerController
) : ViewModel() {

    private val _presets = MutableStateFlow<List<String>>(emptyList())
    val presets: StateFlow<List<String>> = _presets.asStateFlow()

    fun initEqualizer() {
        val sessionId = playerController.audioSessionId
        if (sessionId != 0) {
            equalizerHelper.initEqualizer(sessionId)
            _presets.value = equalizerHelper.getPresetNames()
        }
    }

    fun setBandLevel(band: Short, level: Short) {
        equalizerHelper.setBandLevel(band, level)
    }

    fun applyPreset(index: Short) {
        equalizerHelper.applyPreset(index)
    }

    override fun onCleared() {
        super.onCleared()
        equalizerHelper.release()
    }
}
