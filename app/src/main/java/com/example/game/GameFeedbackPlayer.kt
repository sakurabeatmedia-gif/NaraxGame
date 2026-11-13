package com.example.game

import android.media.AudioManager
import android.media.ToneGenerator

class GameFeedbackPlayer {
    private var toneGenerator: ToneGenerator? = null

    private fun getToneGenerator(): ToneGenerator? {
        if (toneGenerator == null) {
            toneGenerator = try {
                ToneGenerator(AudioManager.STREAM_MUSIC, 45)
            } catch (_: Throwable) {
                null
            }
        }
        return toneGenerator
    }

    fun playMatchPop(enabled: Boolean) {
        if (!enabled) return
        try {
            getToneGenerator()?.startTone(ToneGenerator.TONE_PROP_ACK, 70)
        } catch (_: Throwable) {
        }
    }

    fun playInvalidShake(enabled: Boolean) {
        if (!enabled) return
        try {
            getToneGenerator()?.startTone(ToneGenerator.TONE_PROP_NACK, 90)
        } catch (_: Throwable) {
        }
    }

    fun playLevelUp(enabled: Boolean) {
        if (!enabled) return
        try {
            getToneGenerator()?.startTone(ToneGenerator.TONE_PROP_BEEP2, 160)
        } catch (_: Throwable) {
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
        } catch (_: Throwable) {
        }
        toneGenerator = null
    }
}
