// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auralis.player.core.common.format.formatDuration
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.PlaybackState
import kotlinx.coroutines.launch

/**
 * Barra de reproduccion compacta (mini player) reutilizable.
 *
 * Diseno minimalista y ligero: caratula, metadatos, boton de play/pausa y una
 * linea de progreso fina que ademas es interactiva: se puede pulsar o arrastrar
 * para saltar a cualquier punto de la pista, mostrando el tiempo solo mientras
 * se esta ajustando. El swipe horizontal sobre la fila salta de pista. Se
 * comparte en todas las listas de la aplicacion para mantener una apariencia
 * uniforme.
 */
@Composable
fun MiniPlayerBar(
    state: PlaybackState,
    onPlayPauseClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing,
    modifier: Modifier = Modifier,
) {
    val duration = state.durationMs ?: 0L
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubMs by remember { mutableFloatStateOf(0f) }
    val displayPosition = if (isScrubbing) scrubMs else state.positionMs.toFloat()
    val progress = if (duration > 0) {
        (displayPosition / duration.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val swipeOffsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Surface(
        onClick = onPlayerClick,
        modifier = modifier.fillMaxWidth(),
        shape = ReproductorThemeTokens.shapes.medium,
        color = colors.surfaceRaised,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { },
                            onHorizontalDrag = { _, dragAmount ->
                                scope.launch {
                                    swipeOffsetX.snapTo(swipeOffsetX.value + dragAmount * 0.8f)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    if (swipeOffsetX.value > 150f) {
                                        onSkipPrevious()
                                        launch {
                                            swipeOffsetX.animateTo(800f, tween(150, easing = LinearOutSlowInEasing))
                                            swipeOffsetX.snapTo(-800f)
                                            swipeOffsetX.animateTo(0f, spring(stiffness = Spring.StiffnessHigh))
                                        }
                                    } else if (swipeOffsetX.value < -150f) {
                                        onSkipNext()
                                        launch {
                                            swipeOffsetX.animateTo(-800f, tween(150, easing = LinearOutSlowInEasing))
                                            swipeOffsetX.snapTo(800f)
                                            swipeOffsetX.animateTo(0f, spring(stiffness = Spring.StiffnessHigh))
                                        }
                                    } else {
                                        swipeOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                                    }
                                }
                            },
                            onDragCancel = { scope.launch { swipeOffsetX.animateTo(0f) } }
                        )
                    }
                    .graphicsLayer { translationX = swipeOffsetX.value }
                    .padding(horizontal = spacing.m, vertical = spacing.s),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuralisArtwork(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(ReproductorThemeTokens.shapes.small),
                    artworkReference = state.currentSong?.coverReference
                )

                Spacer(modifier = Modifier.width(spacing.m))

                AnimatedContent(
                    targetState = state.currentSong,
                    transitionSpec = {
                        fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                    },
                    modifier = Modifier.weight(1f),
                    label = "SongMetadataTransition"
                ) { song ->
                    Column {
                        Text(
                            text = song?.title ?: "",
                            style = AppType.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isScrubbing) {
                                "${formatDuration(scrubMs.toLong())} / ${formatDuration(duration)}"
                            } else {
                                song?.artist?.uppercase() ?: "UNKNOWN"
                            },
                            style = AppType.label.copy(fontSize = 10.sp),
                            color = if (isScrubbing) colors.accent else colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(onClick = onPlayPauseClick) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pausa" else "Reproducir",
                        tint = colors.textPrimary
                    )
                }
            }

            if (duration > 0) {
                val barHeight = if (isScrubbing) 5.dp else 3.dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .pointerInput(duration) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                down.consume()
                                isScrubbing = true
                                scrubMs = (down.position.x / size.width).coerceIn(0f, 1f) * duration
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    scrubMs = (change.position.x / size.width).coerceIn(0f, 1f) * duration
                                    change.consume()
                                    if (!change.pressed) break
                                }
                                onSeek(scrubMs.toLong())
                                isScrubbing = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight)
                            .clip(CircleShape)
                            .background(colors.outline.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(colors.accent)
                        )
                    }
                }
            }
        }
    }
}
