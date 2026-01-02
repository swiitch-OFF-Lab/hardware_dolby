/*
 * Copyright (C) 2024-2025 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.lunaris.dolby.R
import org.lunaris.dolby.data.DolbyRepository
import org.lunaris.dolby.domain.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EqualizerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DolbyRepository(application)
    private val context = application

    private val _uiState = MutableStateFlow<EqualizerUiState>(EqualizerUiState.Loading)
    val uiState: StateFlow<EqualizerUiState> = _uiState.asStateFlow()

    private var currentProfile = 0

    init {
        loadEqualizer()
    }

    fun loadEqualizer() {
        viewModelScope.launch {
            try {
                currentProfile = repository.getCurrentProfile()
                val bandGains = repository.getEqualizerGains(currentProfile)
                
                val builtInPresets = getBuiltInPresets()
                val userPresets = repository.getUserPresets()
                val allPresets = userPresets + builtInPresets
                
                val currentPresetName = repository.getPresetName(currentProfile)
                val currentPreset = allPresets.find { it.name == currentPresetName }
                    ?: EqualizerPreset(
                        name = context.getString(R.string.dolby_preset_custom),
                        bandGains = bandGains,
                        isCustom = true
                    )
                
                _uiState.value = EqualizerUiState.Success(
                    presets = allPresets,
                    currentPreset = currentPreset,
                    bandGains = bandGains
                )
            } catch (e: Exception) {
                _uiState.value = EqualizerUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun getBuiltInPresets(): List<EqualizerPreset> {
        val names = context.resources.getStringArray(R.array.dolby_preset_entries)
        val values = context.resources.getStringArray(R.array.dolby_preset_values)
        
        return names.mapIndexed { index, name ->
            val gains = values[index].split(",").map { it.toInt() }
            val tenBandGains = gains.filterIndexed { i, _ -> i % 2 == 0 }
            EqualizerPreset(
                name = name,
                bandGains = DolbyRepository.BAND_FREQUENCIES.mapIndexed { i, freq ->
                    BandGain(frequency = freq, gain = tenBandGains.getOrElse(i) { 0 })
                }
            )
        }
    }

    fun setPreset(preset: EqualizerPreset) {
        viewModelScope.launch {
            repository.setEqualizerGains(currentProfile, preset.bandGains)
            loadEqualizer()
        }
    }

    fun setBandGain(index: Int, gain: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is EqualizerUiState.Success) {
                val newBandGains = state.bandGains.toMutableList()
                newBandGains[index] = newBandGains[index].copy(gain = gain)
                repository.setEqualizerGains(currentProfile, newBandGains)
                loadEqualizer()
            }
        }
    }

    fun savePreset(name: String): String? {
        val state = _uiState.value
        if (state !is EqualizerUiState.Success) return "Invalid state"
        
        if (state.presets.any { it.name.equals(name.trim(), ignoreCase = true) }) {
            return context.getString(R.string.dolby_geq_preset_name_exists)
        }
        
        if (name.length > 50) {
            return context.getString(R.string.dolby_geq_preset_name_too_long)
        }
        
        viewModelScope.launch {
            repository.addUserPreset(name.trim(), state.bandGains)
            loadEqualizer()
        }
        
        return null
    }

    fun deletePreset(preset: EqualizerPreset) {
        if (!preset.isUserDefined) return
        
        viewModelScope.launch {
            repository.deleteUserPreset(preset.name)
            loadEqualizer()
        }
    }

    fun resetGains() {
        viewModelScope.launch {
            val flatPreset = getBuiltInPresets().first()
            repository.setEqualizerGains(currentProfile, flatPreset.bandGains)
            loadEqualizer()
        }
    }
}
