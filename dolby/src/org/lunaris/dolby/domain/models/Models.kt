/*
 * Copyright (C) 2024-2025 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby.domain.models

data class DolbyProfile(
    val id: Int,
    val nameResId: Int
)

data class DolbySettings(
    val enabled: Boolean = true,
    val currentProfile: Int = 0,
    val bassEnhancerEnabled: Boolean = false,
    val volumeLevelerEnabled: Boolean = false
)

data class ProfileSettings(
    val profile: Int,
    val ieqPreset: Int = 0,
    val headphoneVirtualizerEnabled: Boolean = false,
    val speakerVirtualizerEnabled: Boolean = false,
    val stereoWideningAmount: Int = 32,
    val dialogueEnhancerEnabled: Boolean = false,
    val dialogueEnhancerAmount: Int = 6,
    val bassLevel: Int = 0,
    val trebleLevel: Int = 0,
    val bassCurve: Int = 0
)

data class EqualizerPreset(
    val name: String,
    val bandGains: List<BandGain>,
    val isUserDefined: Boolean = false,
    val isCustom: Boolean = false
)

data class BandGain(
    val frequency: Int,
    val gain: Int = 0
)

sealed class DolbyUiState {
    object Loading : DolbyUiState()
    data class Success(
        val settings: DolbySettings,
        val profileSettings: ProfileSettings,
        val currentPresetName: String,
        val isOnSpeaker: Boolean
    ) : DolbyUiState()
    data class Error(val message: String) : DolbyUiState()
}

sealed class EqualizerUiState {
    object Loading : EqualizerUiState()
    data class Success(
        val presets: List<EqualizerPreset>,
        val currentPreset: EqualizerPreset,
        val bandGains: List<BandGain>
    ) : EqualizerUiState()
    data class Error(val message: String) : EqualizerUiState()
}
