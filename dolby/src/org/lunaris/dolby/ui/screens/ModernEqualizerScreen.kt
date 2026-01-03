/*
 * Copyright (C) 2024-2025 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.lunaris.dolby.R
import org.lunaris.dolby.domain.models.EqualizerUiState
import org.lunaris.dolby.ui.components.*
import org.lunaris.dolby.ui.viewmodel.EqualizerViewModel

enum class EqualizerViewMode {
    CURVE,
    SLIDERS
}

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
    var viewMode by remember { mutableStateOf(EqualizerViewMode.CURVE) }
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
                    viewMode = viewMode,
                    onViewModeChange = { viewMode = it },
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
    viewMode: EqualizerViewMode,
    onViewModeChange: (EqualizerViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFlatPreset = state.currentPreset.name == stringResource(R.string.dolby_preset_default)
    val isActive = !isFlatPreset
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Equalizer View",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ViewModeTile(
                        title = "Curve",
                        icon = Icons.Default.ShowChart,
                        isSelected = viewMode == EqualizerViewMode.CURVE,
                        onClick = { onViewModeChange(EqualizerViewMode.CURVE) },
                        modifier = Modifier.weight(1f)
                    )
                    
                    ViewModeTile(
                        title = "Sliders",
                        icon = Icons.Default.Tune,
                        isSelected = viewMode == EqualizerViewMode.SLIDERS,
                        onClick = { onViewModeChange(EqualizerViewMode.SLIDERS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        AnimatedContent(
            targetState = viewMode,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                fadeOut(animationSpec = tween(300))
            },
            label = "equalizer_view_transition"
        ) { mode ->
            when (mode) {
                EqualizerViewMode.CURVE -> {
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
                
                EqualizerViewMode.SLIDERS -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Frequency Response",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                FrequencyResponseCurve(
                                    bandGains = state.bandGains,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
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
                                    text = stringResource(R.string.dolby_geq_slider_label_gain),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    itemsIndexed(state.bandGains) { index, bandGain ->
                                        ModernEqualizerBand(
                                            frequency = bandGain.frequency,
                                            gain = bandGain.gain,
                                            onGainChange = { newGain ->
                                                viewModel.setBandGain(index, newGain)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun ViewModeTile(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = if (isSelected)
            RoundedCornerShape(50.dp)
        else
            RoundedCornerShape(16.dp),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = if (isSelected)
                    RoundedCornerShape(50.dp)
                else
                    RoundedCornerShape(12.dp),
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
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
private fun ModernEqualizerBand(
    frequency: Int,
    gain: Int,
    onGainChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember(gain) { mutableFloatStateOf(gain / 10f) }

    Column(
        modifier = modifier
            .width(64.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "%.1f".format(sliderValue),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = {
                onGainChange((sliderValue * 10).toInt())
            },
            valueRange = -15f..15f,
            modifier = Modifier
                .graphicsLayer {
                    rotationZ = 270f
                    transformOrigin = TransformOrigin(0f, 0f)
                }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(
                        Constraints(
                            minWidth = constraints.minHeight,
                            maxWidth = constraints.maxHeight,
                            minHeight = constraints.minWidth,
                            maxHeight = constraints.maxHeight,
                        )
                    )
                    layout(placeable.height, placeable.width) {
                        placeable.place(-placeable.width, 0)
                    }
                }
                .weight(1f)
                .width(48.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        
        Text(
            text = if (frequency >= 1000) "${frequency / 1000}k" else "$frequency",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FrequencyResponseCurve(
    bandGains: List<org.lunaris.dolby.domain.models.BandGain>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    
    Canvas(modifier = modifier.background(surfaceColor.copy(alpha = 0.3f))) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        drawLine(
            color = surfaceColor,
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 2f
        )
        
        for (i in 1..4) {
            val y = (height / 5) * i
            drawLine(
                color = surfaceColor.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }
        
        if (bandGains.isNotEmpty()) {
            val path = Path()
            val stepX = width / (bandGains.size - 1)
            
            bandGains.forEachIndexed { index, bandGain ->
                val x = index * stepX
                val normalizedGain = (bandGain.gain / 150f).coerceIn(-1f, 1f)
                val y = centerY - (normalizedGain * centerY * 0.8f)
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    val prevX = (index - 1) * stepX
                    val prevGain = bandGains[index - 1].gain
                    val prevNormalizedGain = (prevGain / 150f).coerceIn(-1f, 1f)
                    val prevY = centerY - (prevNormalizedGain * centerY * 0.8f)
                    
                    val cpX1 = prevX + stepX * 0.4f
                    val cpY1 = prevY
                    val cpX2 = x - stepX * 0.4f
                    val cpY2 = y
                    
                    path.cubicTo(cpX1, cpY1, cpX2, cpY2, x, y)
                }
            }
            
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 4f)
            )
            
            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.3f),
                        primaryColor.copy(alpha = 0.05f)
                    )
                )
            )
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
    EnhancedBottomNavigationBar(
        currentRoute = currentRoute,
        onNavigate = onNavigate
    )
}
