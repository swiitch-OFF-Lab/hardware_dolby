/*
 * Copyright (C) 2024-2025 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby

import android.util.Log

object DolbyConstants {

    const val TAG = "Dolby"
    
    const val PREF_ENABLE = "dolby_enable"
    const val PREF_PROFILE = "dolby_profile"
    const val PREF_PRESET = "dolby_preset"
    const val PREF_IEQ = "dolby_ieq"
    const val PREF_HP_VIRTUALIZER = "dolby_virtualizer"
    const val PREF_SPK_VIRTUALIZER = "dolby_spk_virtualizer"
    const val PREF_STEREO_WIDENING = "dolby_stereo_widening"
    const val PREF_DIALOGUE = "dolby_dialogue_enabled"
    const val PREF_DIALOGUE_AMOUNT = "dolby_dialogue_amount"
    const val PREF_BASS = "dolby_bass"
    const val PREF_BASS_LEVEL = "dolby_bass_level"
    const val PREF_BASS_CURVE = "dolby_bass_curve"
    const val PREF_TREBLE = "dolby_treble"
    const val PREF_TREBLE_LEVEL = "dolby_treble_level"
    const val PREF_VOLUME = "dolby_volume"
    const val PREF_PRESETS_MIGRATED = "presets_migrated"
    
    const val PREF_FILE_PRESETS = "presets"

    enum class DsParam(val id: Int, val length: Int = 1) {
        HEADPHONE_VIRTUALIZER(101),
        SPEAKER_VIRTUALIZER(102),
        VOLUME_LEVELER_ENABLE(103),
        IEQ_PRESET(104),
        DIALOGUE_ENHANCER_ENABLE(105),
        DIALOGUE_ENHANCER_AMOUNT(108),
        GEQ_BAND_GAINS(110, 20),
        BASS_ENHANCER_ENABLE(111),
        STEREO_WIDENING_AMOUNT(113);

        override fun toString(): String = "${name}(${id})"
    }

    fun dlog(tag: String, msg: String) {
        if (Log.isLoggable(TAG, Log.DEBUG) || Log.isLoggable(tag, Log.DEBUG)) {
            Log.d("$TAG-$tag", msg)
        }
    }
}
