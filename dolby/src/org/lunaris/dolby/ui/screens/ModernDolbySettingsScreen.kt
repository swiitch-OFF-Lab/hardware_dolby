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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDolbySettingsScreen(
    viewModel: org.lunaris.dolby.ui.viewmodel.DolbyViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    val currentRoute by navController.currentBackStackEntryFlow.collectAsState(null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.dolby_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(
                            Icons.Default.RestartAlt, 
                            contentDescription = "Reset",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (uiState is DolbyUiState.Success) {
                BottomNavigationBar(
                    currentRoute = currentRoute?.destination?.route ?: Screen.Settings.route,
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            is DolbyUiState.Success -> {
                ModernDolbySettingsContent(
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

    if (showResetDialog) {
        ModernConfirmDialog(
            title = stringResource(R.string.dolby_reset_all),
            message = stringResource(R.string.dolby_reset_all_message),
            icon = Icons.Default.RestartAlt,
            onConfirm = {
                viewModel.resetAllProfiles()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }
}

@Composable
private fun ModernDolbySettingsContent(
    state: DolbyUiState.Success,
    viewModel: org.lunaris.dolby.ui.viewmodel.DolbyViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DolbyMainCard(
                enabled = state.settings.enabled,
                onEnabledChange = { viewModel.setDolbyEnabled(it) }
            )
        }

        item {
            AnimatedVisibility(
                visible = state.settings.enabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ModernProfileSelector(
                    currentProfile = state.settings.currentProfile,
                    onProfileChange = { viewModel.setProfile(it) }
                )
            }
        }

        item {
            AnimatedVisibility(
                visible = state.settings.enabled && state.settings.currentProfile != 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ModernSettingsCard(
                    title = "Intelligent Equalizer",
                    icon = Icons.Default.GraphicEq
                ) {
                    ModernIeqSelector(
                        currentPreset = state.profileSettings.ieqPreset,
                        onPresetChange = { viewModel.setIeqPreset(it) }
                    )
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
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp
    ) {
        NavigationBarItem(
            icon = { 
                Icon(
                    Icons.Default.Home,
                    contentDescription = null
                )
            },
            label = { Text("Home") },
            selected = currentRoute == Screen.Settings.route,
            onClick = { onNavigate(Screen.Settings.route) }
        )
        
        NavigationBarItem(
            icon = { 
                AnimatedEqualizerIconDynamic(
                    color = if (currentRoute == Screen.Equalizer.route) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    size = 24.dp
                )
            },
            label = { Text("Equalizer") },
            selected = currentRoute == Screen.Equalizer.route,
            onClick = { onNavigate(Screen.Equalizer.route) }
        )
        
        NavigationBarItem(
            icon = { 
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null
                )
            },
            label = { Text("Advanced") },
            selected = currentRoute == Screen.Advanced.route,
            onClick = { onNavigate(Screen.Advanced.route) }
        )
    }
}
