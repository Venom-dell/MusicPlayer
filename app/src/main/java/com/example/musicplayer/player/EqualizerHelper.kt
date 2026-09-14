package com.example.musicplayer.player

import android.media.audiofx.Equalizer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EqualizerHelper @Inject constructor() {

    private var equalizer: Equalizer? = null

    // We assume 6 bands as requested. Native equalizer might have a different number (typically 5 on Android).
    // The exact bands depend on the hardware. We will use the available bands up to 6.

    fun initEqualizer(audioSessionId: Int) {
        if (equalizer != null) {
            equalizer?.release()
        }
        try {
            equalizer = Equalizer(0, audioSessionId)
            equalizer?.enabled = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBandLevel(bandIndex: Short, level: Short) {
        try {
            equalizer?.setBandLevel(bandIndex, level)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPresetNames(): List<String> {
        val names = mutableListOf<String>()
        equalizer?.let {
            val numPresets = it.numberOfPresets
            for (i in 0 until numPresets) {
                names.add(it.getPresetName(i.toShort()))
            }
        }
        return names
    }

    fun applyPreset(presetIndex: Short) {
        try {
            equalizer?.usePreset(presetIndex)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Example mapping of generic Android presets to Spotify-like names if needed,
    // but Android has native Acoustic, Bass Booster, Dance, etc.

    fun release() {
        equalizer?.release()
        equalizer = null
    }
}
