/*
 * Copyright (C) 2024-2025 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.lunaris.dolby.data.DolbyRepository
import org.lunaris.dolby.domain.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DolbyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DolbyRepository(application)

    private val _uiState = MutableStateFlow<DolbyUiState>(DolbyUiState.Loading)
    val uiState: StateFlow<DolbyUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        
        viewModelScope.launch {
            repository.isOnSpeaker.collect {
                loadSettings()
            }
        }
    }

    fun loadSettings() {
        viewModelScope.launch {
            try {
                val enabled = repository.getDolbyEnabled()
                val profile = repository.getCurrentProfile()
                
                val settings = DolbySettings(
                    enabled = enabled,
                    currentProfile = profile,
                    bassEnhancerEnabled = repository.getBassEnhancerEnabled(profile),
                    volumeLevelerEnabled = repository.getVolumeLevelerEnabled(profile)
                )
                
                val profileSettings = ProfileSettings(
                    profile = profile,
                    ieqPreset = repository.getIeqPreset(profile),
                    headphoneVirtualizerEnabled = repository.getHeadphoneVirtualizerEnabled(profile),
                    speakerVirtualizerEnabled = repository.getSpeakerVirtualizerEnabled(profile),
                    stereoWideningAmount = repository.getStereoWideningAmount(profile),
                    dialogueEnhancerEnabled = repository.getDialogueEnhancerEnabled(profile),
                    dialogueEnhancerAmount = repository.getDialogueEnhancerAmount(profile),
                    bassLevel = repository.getBassLevel(profile),
                    trebleLevel = repository.getTrebleLevel(profile),
                    bassCurve = repository.getBassCurve(profile)
                )
                
                _uiState.value = DolbyUiState.Success(
                    settings = settings,
                    profileSettings = profileSettings,
                    currentPresetName = repository.getPresetName(profile),
                    isOnSpeaker = repository.isOnSpeaker.value
                )
            } catch (e: Exception) {
                _uiState.value = DolbyUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun setDolbyEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDolbyEnabled(enabled)
            loadSettings()
        }
    }

    fun setProfile(profile: Int) {
        viewModelScope.launch {
            repository.setCurrentProfile(profile)
            loadSettings()
        }
    }

    fun setBassEnhancer(enabled: Boolean) {
        viewModelScope.launch {
            val profile = repository.getCurrentProfile()
            repository.setBassEnhancerEnabled(profile, enabled)
            loadSettings()
        }
    }

    fun setBassLevel(level: Int) {
        viewModelScope.launch {
            val profile = repository.getCurrentProfile()
            repository.setBassLevel(profile, level)
            loadSettings()
        }
    }

    fun setBassCurve(curve: Int) {
        viewModelScope.launch {
            val profile = repository.getCurrentProfile()
            repository.setBassCurve(profile, curve)
            loadSettings()
        }
    }

    fun setTrebleLevel(level: Int) {
        viewModelScope.launch {
            val profile = repository.getCurrentProfile()
            repository.setTrebleLevel(profile, level)
            loadSettings()
        }
    }

    fun setVolumeLeveler(enabled: Boolean) {
        viewModelScope.launch {
            val profile = repository.getCurrentProfile()
            repository.setVolumeLevelerEnabled(profile, enabled)
            loadSettings()
        }
    }

    fun setIeqPreset(preset: Int) {
        viewModelScope.launch {
            val profile = repository.getCurrentProfile()
            repository.setIeqPreset(profile, preset)
            loadSettings()
        }
    }

    fun setHeadphoneVirtualizer(enabled: Boolean) {
        viewModelScope.launch {
            val profile = repository.getCurrentProfile()
            repository.setHeadphoneVirtualizerEnabled(profile, enabled)
            loadSettings()
        }
    }

    fun setSpeakerVirtualizer(enabled: Boolean) {
        viewModelScope.launch {
            val profile = repository.getCurrentProfile()
            repository.setSpeakerVirtualizerEnabled(profile, enabled)
            loadSettings()
        }
    }

    fun setStereoWidening(amount: Int) {
        viewModelScope.launch {
            val profile = repository.getCurrentProfile()
            repository.setStereoWideningAmount(profile, amount)
            loadSettings()
        }
    }

    fun setDialogueEnhancer(enabled: Boolean) {
        viewModelScope.launch {
            val profile = repository.getCurrentProfile()
            repository.setDialogueEnhancerEnabled(profile, enabled)
            loadSettings()
        }
    }

    fun setDialogueEnhancerAmount(amount: Int) {
        viewModelScope.launch {
            val profile = repository.getCurrentProfile()
            repository.setDialogueEnhancerAmount(profile, amount)
            loadSettings()
        }
    }

    fun resetAllProfiles() {
        viewModelScope.launch {
            repository.resetAllProfiles()
            loadSettings()
        }
    }

    fun updateSpeakerState() {
        repository.updateSpeakerState()
    }
}
