/*
 * Copyright (C) 2024-2025 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EnhancedBottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EnhancedNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                selected = currentRoute == "settings",
                onClick = { onNavigate("settings") }
            )
            
            EnhancedNavItem(
                icon = Icons.Default.GraphicEq,
                label = "Equalizer",
                selected = currentRoute == "equalizer",
                onClick = { onNavigate("equalizer") },
                isEqualizer = true
            )
            
            EnhancedNavItem(
                icon = Icons.Default.Settings,
                label = "Advanced",
                selected = currentRoute == "advanced",
                onClick = { onNavigate("advanced") }
            )
        }
    }
}

@Composable
private fun EnhancedNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    isEqualizer: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    var isBouncing by remember { mutableStateOf(false) }
    val bounceScale by animateFloatAsState(
        targetValue = if (isBouncing) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = { isBouncing = false },
        label = "bounce_scale"
    )
    
    val backgroundWidth by animateDpAsState(
        targetValue = if (selected) 140.dp else 52.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "background_width"
    )
    
    val backgroundHeight by animateDpAsState(
        targetValue = if (selected) 57.dp else 49.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "background_height"
    )
    
    Box(
        modifier = Modifier
            .height(backgroundHeight)
            .width(backgroundWidth)
            .clip(RoundedCornerShape(if (selected) 50.dp else 16.dp))
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                isBouncing = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.scale(bounceScale),
                contentAlignment = Alignment.Center
            ) {
                if (isEqualizer) {
                    AnimatedEqualizerIconDynamic(
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        size = 24.dp
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(
                    animationSpec = tween(300, delayMillis = 100)
                ) + expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    expandFrom = Alignment.Start
                ),
                exit = fadeOut(
                    animationSpec = tween(200)
                ) + shrinkHorizontally(
                    animationSpec = tween(300),
                    shrinkTowards = Alignment.Start
                )
            ) {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
