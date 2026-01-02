/*
 * Copyright (C) 2024-2025 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch
import org.lunaris.dolby.R
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileCarousel(
    currentProfile: Int,
    onProfileChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val profiles = stringArrayResource(R.array.dolby_profile_entries)
    val profileValues = stringArrayResource(R.array.dolby_profile_values)
    
    val profileIcons = mapOf(
        0 to Icons.Default.AutoAwesome,
        1 to Icons.Default.Movie,
        2 to Icons.Default.MusicNote,
        3 to Icons.Default.Tune
    )
    
    val profileGradients = listOf(
        listOf(Color(0xFF667eea), Color(0xFF764ba2)),
        listOf(Color(0xFFf093fb), Color(0xFFf5576c)),
        listOf(Color(0xFF4facfe), Color(0xFF00f2fe)),
        listOf(Color(0xFFfa709a), Color(0xFFfee140))
    )
    
    val initialPage = profileValues.indexOfFirst { it.toInt() == currentProfile }.coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { profiles.size }
    )
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != initialPage) {
            val selectedValue = profileValues[pagerState.currentPage].toInt()
            onProfileChange(selectedValue)
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.dolby_profile_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentPadding = PaddingValues(horizontal = 64.dp),
                pageSpacing = 8.dp
            ) { page ->
                val profileValue = profileValues[page].toInt()
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                
                ProfileCard(
                    profile = profiles[page],
                    icon = profileIcons[profileValue] ?: Icons.Default.Tune,
                    gradient = profileGradients.getOrElse(profileValue) { profileGradients[0] },
                    isSelected = page == pagerState.currentPage,
                    pageOffset = pageOffset,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(profiles.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    
                    Box(
                        modifier = Modifier
                            .padding(3.dp)
                            .size(
                                width = if (isSelected) 24.dp else 6.dp,
                                height = 6.dp
                            )
                            .clip(CircleShape)
                            .background(
                                if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileCard(
    profile: String,
    icon: ImageVector,
    gradient: List<Color>,
    isSelected: Boolean,
    pageOffset: Float,
    onClick: () -> Unit
) {
    val scale = lerp(
        start = 0.8f,
        stop = 1f,
        fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
    )
    
    val alpha = lerp(
        start = 0.5f,
        stop = 1f,
        fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
    )
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradient
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.85f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "icon_scale"
                )
                
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .scale(iconScale),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.3f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = profile,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (isSelected) {
                    Surface(
                        modifier = Modifier.height(2.dp).width(24.dp),
                        shape = CircleShape,
                        color = Color.White
                    ) {}
                }
            }
            
            if (isSelected) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(24.dp),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = gradient[0],
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
