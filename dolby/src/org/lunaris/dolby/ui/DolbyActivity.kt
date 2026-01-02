/*
 * Copyright (C) 2024-2025 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby.ui

import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import org.lunaris.dolby.ui.screens.DolbyNavHost
import org.lunaris.dolby.ui.theme.DolbyTheme
import org.lunaris.dolby.ui.viewmodel.DolbyViewModel
import org.lunaris.dolby.ui.viewmodel.EqualizerViewModel

class DolbyActivity : ComponentActivity() {

    private val dolbyViewModel: DolbyViewModel by viewModels()
    private val equalizerViewModel: EqualizerViewModel by viewModels()
    
    private val audioManager by lazy { getSystemService(AudioManager::class.java) }
    private val handler = Handler()
    
    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
            dolbyViewModel.updateSpeakerState()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {
            dolbyViewModel.updateSpeakerState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, handler)
        
        setContent {
            DolbyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    DolbyNavHost(
                        dolbyViewModel = dolbyViewModel,
                        equalizerViewModel = equalizerViewModel
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        handler.removeCallbacksAndMessages(null)
    }
}
