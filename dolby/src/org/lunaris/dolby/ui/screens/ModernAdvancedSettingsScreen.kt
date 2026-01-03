/*
 * Copyright (C) 2024-2025 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.lunaris.dolby.R
import org.lunaris.dolby.domain.models.DolbyUiState
import org.lunaris.dolby.ui.components.*
import org.lunaris.dolby.ui.viewmodel.DolbyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernAdvancedSettingsScreen(
    viewModel: DolbyViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentRoute by navController.currentBackStackEntryFlow.collectAsState(null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.dolby_category_adv_settings),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute?.destination?.route ?: Screen.Advanced.route,
                onNavigate = { route ->
                    if (currentRoute?.destination?.route != route) {
                        navController.navigate(route) {
                            popUpTo(Screen.Settings.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is DolbyUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is DolbyUiState.Success -> {
                ModernAdvancedSettingsContent(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is DolbyUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernAdvancedSettingsContent(
    state: DolbyUiState.Success,
    viewModel: DolbyViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.settings.enabled) {
            item {
                ModernSettingsCard(
                    title = stringResource(R.string.dolby_category_settings),
                    icon = Icons.Default.Tune
                ) {
                    Column {
                        ModernSettingSwitch(
                            title = stringResource(R.string.dolby_bass_enhancer),
                            subtitle = stringResource(R.string.dolby_bass_enhancer_summary),
                            checked = state.profileSettings.bassLevel > 0,
                            onCheckedChange = { enabled ->
                                if (enabled && state.profileSettings.bassLevel == 0) {
                                    viewModel.setBassLevel(50)
                                } else if (!enabled) {
                                    viewModel.setBassLevel(0)
                                }
                            },
                            icon = Icons.Default.MusicNote
                        )
                        
                        AnimatedVisibility(visible = state.profileSettings.bassLevel > 0) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                ModernSettingSelector(
                                    title = stringResource(R.string.dolby_bass_curve),
                                    currentValue = state.profileSettings.bassCurve,
                                    entries = R.array.dolby_bass_curve_entries,
                                    values = R.array.dolby_bass_curve_values,
                                    onValueChange = { viewModel.setBassCurve(it) },
                                    icon = Icons.Default.Equalizer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ModernSettingSlider(
                                    title = stringResource(R.string.dolby_bass_level),
                                    value = state.profileSettings.bassLevel,
                                    onValueChange = { viewModel.setBassLevel(it.toInt()) },
                                    valueRange = 0f..100f,
                                    steps = 19,
                                    valueLabel = { "$it%" }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Column {
                        ModernSettingSwitch(
                            title = stringResource(R.string.dolby_treble_enhancer),
                            subtitle = stringResource(R.string.dolby_treble_enhancer_summary),
                            checked = state.profileSettings.trebleLevel > 0,
                            onCheckedChange = { enabled ->
                                if (enabled && state.profileSettings.trebleLevel == 0) {
                                    viewModel.setTrebleLevel(30)
                                } else if (!enabled) {
                                    viewModel.setTrebleLevel(0)
                                }
                            },
                            icon = Icons.Default.GraphicEq
                        )

                        AnimatedVisibility(visible = state.profileSettings.trebleLevel > 0) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                ModernSettingSlider(
                                    title = stringResource(R.string.dolby_treble_level),
                                    value = state.profileSettings.trebleLevel,
                                    onValueChange = { viewModel.setTrebleLevel(it.toInt()) },
                                    valueRange = 0f..100f,
                                    steps = 19,
                                    valueLabel = { "$it%" }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    ModernSettingSwitch(
                        title = stringResource(R.string.dolby_volume_leveler),
                        subtitle = stringResource(R.string.dolby_volume_leveler_summary),
                        checked = state.settings.volumeLevelerEnabled,
                        onCheckedChange = { viewModel.setVolumeLeveler(it) },
                        icon = Icons.Default.BarChart
                    )
                }
            }
            
            if (state.settings.currentProfile != 0) {
                item {
                    ModernSettingsCard(
                        title = "Surround Virtualizer",
                        icon = Icons.Default.Headphones
                    ) {
                        if (state.isOnSpeaker) {
                            ModernSettingSwitch(
                                title = stringResource(R.string.dolby_spk_virtualizer),
                                subtitle = stringResource(R.string.dolby_spk_virtualizer_summary),
                                checked = state.profileSettings.speakerVirtualizerEnabled,
                                onCheckedChange = { viewModel.setSpeakerVirtualizer(it) },
                                icon = Icons.Default.Speaker
                            )
                        } else {
                            ModernSettingSwitch(
                                title = stringResource(R.string.dolby_hp_virtualizer),
                                subtitle = stringResource(R.string.dolby_hp_virtualizer_summary),
                                checked = state.profileSettings.headphoneVirtualizerEnabled,
                                onCheckedChange = { viewModel.setHeadphoneVirtualizer(it) },
                                icon = Icons.Default.Headphones
                            )
                            
                            AnimatedVisibility(visible = state.profileSettings.headphoneVirtualizerEnabled) {
                                Column {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    ModernSettingSlider(
                                        title = stringResource(R.string.dolby_hp_virtualizer_dolby_strength),
                                        value = state.profileSettings.stereoWideningAmount,
                                        onValueChange = { viewModel.setStereoWidening(it.toInt()) },
                                        valueRange = 4f..64f,
                                        steps = 59
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    ModernSettingsCard(
                        title = "Dialogue Enhancement",
                        icon = Icons.Default.RecordVoiceOver
                    ) {
                        ModernSettingSwitch(
                            title = stringResource(R.string.dolby_dialogue_enhancer),
                            subtitle = stringResource(R.string.dolby_dialogue_enhancer_summary),
                            checked = state.profileSettings.dialogueEnhancerEnabled,
                            onCheckedChange = { viewModel.setDialogueEnhancer(it) },
                            icon = Icons.Default.RecordVoiceOver
                        )
                        
                        AnimatedVisibility(visible = state.profileSettings.dialogueEnhancerEnabled) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                ModernSettingSlider(
                                    title = stringResource(R.string.dolby_dialogue_enhancer_dolby_strength),
                                    value = state.profileSettings.dialogueEnhancerAmount,
                                    onValueChange = { viewModel.setDialogueEnhancerAmount(it.toInt()) },
                                    valueRange = 1f..12f,
                                    steps = 10
                                )
                            }
                        }
                    }
                }
            }
        } else if (state.settings.currentProfile == 0 && state.settings.enabled) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.dolby_adv_settings_footer),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        } else if (!state.settings.enabled) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Enable Dolby Atmos to access advanced settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    EnhancedBottomNavigationBar(
        currentRoute = currentRoute,
        onNavigate = onNavigate
    )
}
