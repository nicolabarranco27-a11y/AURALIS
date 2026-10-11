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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
 * Muestra caratula, metadatos, progreso y controles, con swipe para
 * saltar de pista. Compartida por las listas de la aplicacion.
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
    var sliderValue by remember { mutableFloatStateOf(state.positionMs.toFloat()) }
    var isDragging by remember { mutableStateOf(false) }
    val swipeOffsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Surface(
        onClick = onPlayerClick,
        modifier = modifier.fillMaxWidth(),
        shape = ReproductorThemeTokens.shapes.medium,
        color = colors.surfaceRaised,
        tonalElevation = 8.dp
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
                        .size(40.dp)
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
                            style = AppType.body.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song?.artist?.uppercase() ?: "UNKNOWN",
                            style = AppType.label.copy(fontSize = 10.sp),
                            color = colors.textSecondary,
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

            if (state.durationMs != null && state.durationMs > 0) {
                Slider(
                    value = if (isDragging) sliderValue else state.positionMs.toFloat(),
                    onValueChange = {
                        isDragging = true
                        sliderValue = it
                    },
                    onValueChangeFinished = {
                        isDragging = false
                        onSeek(sliderValue.toLong())
                    },
                    valueRange = 0f..state.durationMs.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .padding(horizontal = spacing.m),
                    colors = SliderDefaults.colors(
                        thumbColor = colors.textPrimary,
                        activeTrackColor = colors.accent,
                        inactiveTrackColor = colors.outline
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = spacing.m, end = spacing.m, bottom = spacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(if (isDragging) sliderValue.toLong() else state.positionMs),
                        style = AppType.timer,
                        color = colors.textSecondary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = formatDuration(state.durationMs),
                        style = AppType.timer,
                        color = colors.textSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
