/*
 * Copyright (C) 2024-2025 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.pow

@Composable
fun AnimatedEqualizerIconDynamic(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    size: Dp = 24.dp,
    barCount: Int = 5
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer_dynamic")
    
    val barHeights = List(barCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 800 + (index * 50),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_height_$index"
        )
    }

    Canvas(modifier = modifier.size(size)) {
        val canvasWidth = this.size.width
        val canvasHeight = this.size.height
        
        val barWidths = List(barCount) { index ->
            val normalizedPosition = index.toFloat() / (barCount - 1)
            val centerOffset = (normalizedPosition - 0.5f) * 2
            val widthFactor = 1.0f - (centerOffset * centerOffset).pow(0.6f)
            val scaledWidth = 0.5f + (widthFactor * 0.5f)
            scaledWidth
        }
        
        val totalWidthFactor = barWidths.sum() + (barCount - 1) * 0.3f
        val baseBarWidth = canvasWidth / totalWidthFactor
        
        var currentX = 0f
        
        barHeights.forEachIndexed { index, heightAnimation ->
            val barWidth = baseBarWidth * barWidths[index]
            val barHeight = canvasHeight * heightAnimation.value
            val y = canvasHeight - barHeight

            drawRoundRect(
                color = color,
                topLeft = Offset(currentX, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
            
            currentX += barWidth + (baseBarWidth * 0.3f)
        }
    }
}

@Composable
fun AnimatedEqualizerHeader(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    width: Dp = 120.dp,
    height: Dp = 56.dp,
    barCount: Int = 9
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer_header")
    
    val barHeights = List(barCount) { index ->
        val normalizedPosition = index.toFloat() / (barCount - 1)
        val centerOffset = kotlin.math.abs((normalizedPosition - 0.5f) * 2)
        val heightScale = 1.0f - (centerOffset * centerOffset * 0.6f)
        
        infiniteTransition.animateFloat(
            initialValue = 0.25f * heightScale,
            targetValue = 0.95f * heightScale,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 900 + (index * 60),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_height_$index"
        )
    }

    Canvas(modifier = modifier.size(width = width, height = height)) {
        val canvasWidth = this.size.width
        val canvasHeight = this.size.height
        
        val barWidths = List(barCount) { index ->
            val normalizedPosition = index.toFloat() / (barCount - 1)
            val centerOffset = (normalizedPosition - 0.5f) * 2
            val widthFactor = 1.0f - (centerOffset * centerOffset).pow(0.7f)
            val scaledWidth = 0.45f + (widthFactor * 0.55f)
            scaledWidth
        }
        
        val totalWidthFactor = barWidths.sum() + (barCount - 1) * 0.25f
        val baseBarWidth = canvasWidth / totalWidthFactor
        
        var currentX = 0f
        
        barHeights.forEachIndexed { index, heightAnimation ->
            val barWidth = baseBarWidth * barWidths[index]
            val barHeight = canvasHeight * heightAnimation.value
            val y = canvasHeight - barHeight

            drawRoundRect(
                color = color.copy(alpha = 0.85f),
                topLeft = Offset(currentX, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
            
            currentX += barWidth + (baseBarWidth * 0.25f)
        }
    }
}
