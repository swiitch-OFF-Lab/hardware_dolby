/*
 * Copyright (C) 2024-2025 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.lunaris.dolby.data.DolbyRepository

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        when (intent.action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_BOOT_COMPLETED -> {
                try {
                    val repository = DolbyRepository(context)
                    val enabled = repository.getDolbyEnabled()
                    val profile = repository.getCurrentProfile()
                    
                    if (enabled) {
                        repository.setDolbyEnabled(true)
                        repository.setCurrentProfile(profile)
                    }
                    
                    Log.d(TAG, "Dolby initialized: enabled=$enabled, profile=$profile")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize Dolby", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "Dolby-Boot"
    }
}
