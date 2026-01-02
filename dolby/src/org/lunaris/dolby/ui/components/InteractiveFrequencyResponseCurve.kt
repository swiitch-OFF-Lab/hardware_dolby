/*
 * Copyright (C) 2024-2025 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lunaris.dolby.domain.models.BandGain
import kotlin.math.abs

@Composable
fun InteractiveFrequencyResponseCurve(
    bandGains: List<BandGain>,
    onBandGainChange: (index: Int, newGain: Int) -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    
    val backgroundColor = if (isActive) {
        primaryContainerColor
    } else {
        surfaceColor.copy(alpha = 0.2f)
    }
    
    val borderColor = if (isActive) {
        primaryColor
    } else {
        Color.Transparent
    }
    
    val borderWidth = if (isActive) 2.dp else 0.dp
    
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    
    val controlPoints = remember(bandGains) {
        mutableStateListOf(*bandGains.map { it.gain }.toTypedArray())
    }
    
    LaunchedEffect(bandGains) {
        bandGains.forEachIndexed { index, bandGain ->
            if (controlPoints.getOrNull(index) != bandGain.gain) {
                controlPoints[index] = bandGain.gain
            }
        }
    }
    
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width
                            val height = size.height
                            val stepX = width / (bandGains.size - 1).toFloat()
                            
                            var closestIndex = -1
                            var closestDistance = Float.MAX_VALUE
                            
                            bandGains.forEachIndexed { index, _ ->
                                val x = index * stepX
                                val normalizedGain = (controlPoints[index] / 150f).coerceIn(-1f, 1f)
                                val y = height / 2 - (normalizedGain * height / 2 * 0.85f)
                                
                                val distance = abs(offset.x - x) + abs(offset.y - y)
                                if (distance < closestDistance && distance < 80f) {
                                    closestDistance = distance
                                    closestIndex = index
                                }
                            }
                            
                            if (closestIndex != -1) {
                                draggedIndex = closestIndex
                            }
                        },
                        onDrag = { change, _ ->
                            draggedIndex?.let { index ->
                                val height = size.height
                                val centerY = height / 2
                                val y = change.position.y
                                val normalizedGain = ((centerY - y) / (height / 2 * 0.85f)).coerceIn(-1f, 1f)
                                val newGain = (normalizedGain * 150).toInt().coerceIn(-150, 150)
                                
                                if (controlPoints[index] != newGain) {
                                    controlPoints[index] = newGain
                                }
                            }
                        },
                        onDragEnd = {
                            draggedIndex?.let { index ->
                                onBandGainChange(index, controlPoints[index])
                            }
                            draggedIndex = null
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            
            val gridColor = if (isActive) {
                surfaceColor.copy(alpha = 0.4f)
            } else {
                surfaceColor
            }
            
            val gridVerticalColor = if (isActive) {
                surfaceColor.copy(alpha = 0.3f)
            } else {
                surfaceColor.copy(alpha = 0.2f)
            }
            
            drawLine(
                color = gridColor,
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 2f
            )
            
            for (i in 1..4) {
                val y = (height / 5) * i
                drawLine(
                    color = gridColor.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }
            
            val stepX = width / (bandGains.size - 1)
            bandGains.forEachIndexed { index, _ ->
                val x = index * stepX
                drawLine(
                    color = gridVerticalColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
            }
            
            if (bandGains.isNotEmpty() && controlPoints.isNotEmpty()) {
                val path = Path()
                
                controlPoints.forEachIndexed { index, gain ->
                    val x = index * stepX
                    val normalizedGain = (gain / 150f).coerceIn(-1f, 1f)
                    val y = centerY - (normalizedGain * centerY * 0.85f)
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        val prevX = (index - 1) * stepX
                        val prevGain = controlPoints[index - 1]
                        val prevNormalizedGain = (prevGain / 150f).coerceIn(-1f, 1f)
                        val prevY = centerY - (prevNormalizedGain * centerY * 0.85f)
                        
                        val cpX1 = prevX + stepX * 0.4f
                        val cpY1 = prevY
                        val cpX2 = x - stepX * 0.4f
                        val cpY2 = y
                        
                        path.cubicTo(cpX1, cpY1, cpX2, cpY2, x, y)
                    }
                }
                
                drawPath(
                    path = path,
                    color = if (isActive) primaryColor else primaryColor.copy(alpha = 0.8f),
                    style = Stroke(width = if (isActive) 5f else 4f)
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
                        colors = if (isActive) {
                            listOf(
                                primaryColor.copy(alpha = 0.4f),
                                primaryColor.copy(alpha = 0.08f)
                            )
                        } else {
                            listOf(
                                primaryColor.copy(alpha = 0.3f),
                                primaryColor.copy(alpha = 0.05f)
                            )
                        }
                    )
                )
                
                controlPoints.forEachIndexed { index, gain ->
                    val x = index * stepX
                    val normalizedGain = (gain / 150f).coerceIn(-1f, 1f)
                    val y = centerY - (normalizedGain * centerY * 0.85f)
                    
                    val isBeingDragged = draggedIndex == index
                    val pointRadius = if (isBeingDragged) 12f else 10f
                    
                    if (isBeingDragged) {
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.3f),
                            radius = 20f,
                            center = Offset(x, y)
                        )
                    }
                    
                    drawCircle(
                        color = Color.White,
                        radius = pointRadius,
                        center = Offset(x, y)
                    )
                    
                    drawCircle(
                        color = if (isActive) primaryColor else primaryColor.copy(alpha = 0.8f),
                        radius = pointRadius - 2f,
                        center = Offset(x, y)
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            bandGains.forEach { bandGain ->
                Text(
                    text = if (bandGain.frequency >= 1000) {
                        "${bandGain.frequency / 1000}k"
                    } else {
                        "${bandGain.frequency}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) {
                        onSurfaceColor.copy(alpha = 0.8f)
                    } else {
                        onSurfaceColor.copy(alpha = 0.7f)
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "+15",
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) {
                    secondaryColor
                } else {
                    secondaryColor.copy(alpha = 0.8f)
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = "0",
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) {
                    secondaryColor
                } else {
                    secondaryColor.copy(alpha = 0.8f)
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "-15",
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) {
                    secondaryColor
                } else {
                    secondaryColor.copy(alpha = 0.8f)
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        
        draggedIndex?.let { index ->
            val gain = controlPoints[index]
            val gainDb = gain / 10f
            
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp),
                color = primaryColor,
                shadowElevation = 4.dp
            ) {
                Text(
                    text = "${if (gainDb >= 0) "+" else ""}%.1f dB".format(gainDb),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
