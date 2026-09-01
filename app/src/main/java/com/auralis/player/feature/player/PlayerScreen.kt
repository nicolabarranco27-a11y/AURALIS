package com.auralis.player.feature.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.player.core.ui.components.AuralisArtwork
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.RepeatMode
import com.auralis.player.domain.model.Song
import kotlinx.coroutines.launch

@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.playbackState.collectAsStateWithLifecycle()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing
    val gradient = ReproductorThemeTokens.primaryGradient
    
    val swipeOffsetX = remember { Animatable(0f) }
    var isNext by remember { mutableStateOf(true) }
    var draggedSongId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Sincronización: Solo reseteamos el offset cuando la nueva canción ya está establecida y visible.
    // Usamos el ID de la canción actual para detectar cambios reales.
    LaunchedEffect(state.currentSong?.id) {
        if (draggedSongId != null) {
            // Si venimos de un swipe, reseteamos el estado de arrastre de forma silenciosa
            // una vez que la transición ha comenzado para la nueva canción.
            draggedSongId = null
            swipeOffsetX.snapTo(0f)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Text("↓", style = AppType.title, color = colors.textPrimary)
            }
            Text(
                text = "REPRODUCIENDO",
                style = AppType.label.copy(letterSpacing = 2.sp),
                color = colors.textSecondary
            )
            IconButton(onClick = { /* TODO: More options */ }) {
                Text("⋮", style = AppType.title, color = colors.textPrimary)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Cover Art Area
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surfaceRaised),
            contentAlignment = Alignment.Center
        ) {
            val widthPx = with(density) { maxWidth.toPx() }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(state.currentSong?.id) {
                        detectHorizontalDragGestures(
                            onDragStart = { 
                                draggedSongId = state.currentSong?.id?.value
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                scope.launch {
                                    swipeOffsetX.snapTo(swipeOffsetX.value + dragAmount * 0.8f)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    if (swipeOffsetX.value > 150f) {
                                        // Anterior
                                        isNext = false
                                        // Animamos hacia afuera y lanzamos el comando
                                        launch {
                                            swipeOffsetX.animateTo(widthPx, tween(250, easing = FastOutLinearInEasing))
                                            viewModel.skipPrevious()
                                        }
                                    } else if (swipeOffsetX.value < -150f) {
                                        // Siguiente
                                        isNext = true
                                        // Animamos hacia afuera y lanzamos el comando
                                        launch {
                                            swipeOffsetX.animateTo(-widthPx, tween(250, easing = FastOutLinearInEasing))
                                            viewModel.skipNext()
                                        }
                                    } else {
                                        // Regreso suave al centro si no se superó el umbral
                                        swipeOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                                        draggedSongId = null
                                    }
                                }
                            },
                            onDragCancel = { 
                                scope.launch { 
                                    swipeOffsetX.animateTo(0f)
                                    draggedSongId = null
                                } 
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = state.currentSong,
                    transitionSpec = {
                        // Determinamos si el cambio es manual analizando si hay un drag activo con offset significativo
                        val wasManual = draggedSongId != null && Math.abs(swipeOffsetX.value) > 100f
                        
                        if (isNext) {
                            (slideInHorizontally { width -> width } + fadeIn(tween(400))).togetherWith(
                                if (wasManual) fadeOut(tween(300)) // La salida manual ya se hizo/está haciendo vía graphicsLayer
                                else slideOutHorizontally { width -> -width } + fadeOut(tween(300))
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn(tween(400))).togetherWith(
                                if (wasManual) fadeOut(tween(300))
                                else slideOutHorizontally { width -> width } + fadeOut(tween(300))
                            )
                        }
                    },
                    label = "ArtworkTransition"
                ) { song ->
                    AuralisArtwork(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                // El desplazamiento manual SOLO se aplica a la canción que causó el drag.
                                // La nueva canción (que tiene un ID diferente) entrará limpia en x=0.
                                if (song?.id?.value == draggedSongId) {
                                    translationX = swipeOffsetX.value
                                }
                            },
                        artworkReference = song?.coverReference
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.currentSong?.title ?: "Sin reproducción",
                style = AppType.title.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(spacing.s))
            Text(
                text = (state.currentSong?.artist ?: "Desconocido").uppercase(),
                style = AppType.label.copy(fontSize = 12.sp, letterSpacing = 2.sp),
                color = colors.accentSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(spacing.xl))

        // Progress
        var sliderValue by remember(state.positionMs) { mutableFloatStateOf(state.positionMs.toFloat()) }
        var isDragging by remember { mutableStateOf(false) }
        val duration = state.durationMs ?: 0L

        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = if (isDragging) sliderValue else state.positionMs.toFloat(),
                onValueChange = {
                    isDragging = true
                    sliderValue = it
                },
                onValueChangeFinished = {
                    isDragging = false
                    viewModel.seekTo(sliderValue.toLong())
                },
                valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                colors = SliderDefaults.colors(
                    thumbColor = colors.textPrimary,
                    activeTrackColor = colors.accent,
                    inactiveTrackColor = colors.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(if (isDragging) sliderValue.toLong() else state.positionMs),
                    style = AppType.timer,
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = formatDuration(duration),
                    style = AppType.timer,
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.xl))

        // Primary Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.toggleShuffle() },
                modifier = Modifier.size(48.dp)
            ) {
                Text(
                    text = "↝",
                    style = AppType.title.copy(fontSize = 24.sp),
                    color = if (state.shuffleEnabled) colors.accent else colors.textSecondary
                )
            }

            IconButton(
                onClick = { 
                    isNext = false
                    // Marcamos como no manual para que use la animación por defecto de slideOut
                    draggedSongId = null
                    viewModel.skipPrevious() 
                },
                modifier = Modifier.size(56.dp)
            ) {
                Text("«", style = AppType.display.copy(fontSize = 32.sp), color = colors.textPrimary)
            }

            Surface(
                onClick = { viewModel.togglePlayPause() },
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier.size(80.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(gradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.isPlaying) "‖" else "▶",
                        style = AppType.display.copy(fontSize = 32.sp),
                        color = Color.White
                    )
                }
            }

            IconButton(
                onClick = { 
                    isNext = true
                    // Marcamos como no manual
                    draggedSongId = null
                    viewModel.skipNext() 
                },
                modifier = Modifier.size(56.dp)
            ) {
                Text("»", style = AppType.display.copy(fontSize = 32.sp), color = colors.textPrimary)
            }

            IconButton(
                onClick = { viewModel.cycleRepeatMode() },
                modifier = Modifier.size(48.dp)
            ) {
                Text(
                    text = when (state.repeatMode) {
                        RepeatMode.ONE -> "⟳₁"
                        RepeatMode.ALL -> "⟳"
                        else -> "→"
                    },
                    style = AppType.title.copy(fontSize = 24.sp),
                    color = if (state.repeatMode != RepeatMode.OFF) colors.accent else colors.textSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(spacing.xl).navigationBarsPadding())
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
