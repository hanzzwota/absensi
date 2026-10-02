package com.example.ui.components

import android.media.AudioManager
import android.media.ToneGenerator

object SoundFeedback {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    fun playSuccessBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        } catch (e: Exception) {
            // ignore audio playback errors
        }
    }

    fun playErrorBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 250)
        } catch (e: Exception) {
            // ignore audio playback errors
        }
    }
}
