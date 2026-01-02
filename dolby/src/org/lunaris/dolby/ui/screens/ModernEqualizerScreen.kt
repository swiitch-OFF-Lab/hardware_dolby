/*
 * Copyright (C) 2024-2025 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.lunaris.dolby.domain.models.EqualizerUiState
import org.lunaris.dolby.ui.components.ModernConfirmDialog
import org.lunaris.dolby.ui.components.InteractiveFrequencyResponseCurve
import org.lunaris.dolby.ui.components.AnimatedEqualizerIconDynamic
import org.lunaris.dolby.ui.viewmodel.EqualizerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernEqualizerScreen(
    viewModel: EqualizerViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSaveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    val currentRoute by navController.currentBackStackEntryFlow.collectAsState(null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.dolby_preset),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset")
                    }
                    if (uiState is EqualizerUiState.Success) {
                        val state = uiState as EqualizerUiState.Success
                        if (state.currentPreset.isUserDefined) {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute?.destination?.route ?: Screen.Equalizer.route,
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
            is EqualizerUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is EqualizerUiState.Success -> {
                ModernEqualizerContent(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is EqualizerUiState.Error -> {
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

    if (showSaveDialog) {
        SavePresetDialog(
            onSave = { name ->
                val error = viewModel.savePreset(name)
                if (error == null) {
                    showSaveDialog = false
                }
                error
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    if (showDeleteDialog && uiState is EqualizerUiState.Success) {
        val state = uiState as EqualizerUiState.Success
        ModernConfirmDialog(
            title = stringResource(R.string.dolby_geq_delete_preset),
            message = stringResource(R.string.dolby_geq_delete_preset_prompt),
            icon = Icons.Default.Delete,
            onConfirm = {
                viewModel.deletePreset(state.currentPreset)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showResetDialog) {
        ModernConfirmDialog(
            title = stringResource(R.string.dolby_geq_reset_gains),
            message = stringResource(R.string.dolby_geq_reset_gains_prompt),
            icon = Icons.Default.RestartAlt,
            onConfirm = {
                viewModel.resetGains()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }
}

@Composable
private fun ModernEqualizerContent(
    state: EqualizerUiState.Success,
    viewModel: EqualizerViewModel,
    modifier: Modifier = Modifier
) {
    val isFlatPreset = state.currentPreset.name == stringResource(R.string.dolby_preset_default)
    val isActive = !isFlatPreset
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            ModernPresetSelector(
                presets = state.presets,
                currentPreset = state.currentPreset,
                onPresetSelected = { viewModel.setPreset(it) }
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Interactive Frequency Response",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Drag the control points to adjust gain (±15 dB)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                InteractiveFrequencyResponseCurve(
                    bandGains = state.bandGains,
                    onBandGainChange = { index, newGain ->
                        viewModel.setBandGain(index, newGain)
                    },
                    isActive = isActive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernPresetSelector(
    presets: List<org.lunaris.dolby.domain.models.EqualizerPreset>,
    currentPreset: org.lunaris.dolby.domain.models.EqualizerPreset,
    onPresetSelected: (org.lunaris.dolby.domain.models.EqualizerPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(20.dp)) {
        Text(
            text = stringResource(R.string.dolby_geq_preset),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (currentPreset.isUserDefined) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = currentPreset.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                presets.forEach { preset ->
                    DropdownMenuItem(
                        text = { 
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(preset.name)
                                if (preset.isUserDefined) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            onPresetSelected(preset)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavePresetDialog(
    onSave: (String) -> String?,
    onDismiss: () -> Unit
) {
    var presetName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { 
            Text(
                stringResource(R.string.dolby_geq_new_preset),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { 
                        presetName = it
                        errorMessage = null
                    },
                    label = { Text(stringResource(R.string.dolby_geq_preset_name)) },
                    isError = errorMessage != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val error = onSave(presetName)
                    if (error != null) {
                        errorMessage = error
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
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
