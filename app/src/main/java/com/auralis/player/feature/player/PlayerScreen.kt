package com.auralis.player.feature.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.player.core.common.format.formatDuration
import com.auralis.player.core.ui.components.AuralisArtwork
import com.auralis.player.core.ui.components.VinylAura
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.data.repository.EqualizerPreferences
import com.auralis.player.domain.model.RepeatMode
import com.auralis.player.domain.model.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.playbackState.collectAsStateWithLifecycle()
    val sleepTimer by viewModel.sleepTimer.collectAsStateWithLifecycle()
    val auraColor by viewModel.artworkAura.collectAsStateWithLifecycle()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing
    val gradient = ReproductorThemeTokens.primaryGradient
    
    val swipeOffsetX = remember { Animatable(0f) }
    var isNext by remember { mutableStateOf(true) }
    var draggedSongId by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showTrackDetailsDialog by remember { mutableStateOf(false) }
    var isDismissing by remember { mutableStateOf(false) }
    
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

    // Si el swipe no cambia de pista (no hay canción adyacente), la carátula quedaría
    // fuera de pantalla: este recentrado de respaldo la devuelve al centro.
    suspend fun recenterAfterSkippedSwipe() {
        delay(350)
        if (draggedSongId != null) {
            swipeOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
            draggedSongId = null
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
            IconButton(
                onClick = {
                    if (!isDismissing) {
                        isDismissing = true
                        onBackClick()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Cerrar reproductor",
                    tint = colors.textPrimary
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.s)
            ) {
                Text(
                    text = "REPRODUCIENDO",
                    style = AppType.label.copy(letterSpacing = 2.sp),
                    color = colors.textSecondary
                )
                AudioEqualizerVisualizer(
                    isPlaying = state.isPlaying,
                    color = auraColor,
                    barMaxHeightDp = 12,
                    barWidthDp = 3
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Más opciones",
                        tint = colors.textPrimary
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .background(colors.surfaceRaised)
                        .border(
                            border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    DropdownMenuItem(
                        text = { Text("Ecualizador", color = colors.textPrimary, style = AppType.body) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Tune, contentDescription = null, tint = colors.accent)
                        },
                        onClick = {
                            showMenu = false
                            showEqualizerSheet = true
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = sleepTimerLabel(sleepTimer),
                                color = colors.textPrimary,
                                style = AppType.body
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.Timer, contentDescription = null, tint = colors.accent)
                        },
                        onClick = {
                            showMenu = false
                            showSleepTimerDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Detalles de la pista", color = colors.textPrimary, style = AppType.body) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Info, contentDescription = null, tint = colors.accent)
                        },
                        onClick = {
                            showMenu = false
                            showTrackDetailsDialog = true
                        }
                    )
                }
            }
        }

        state.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(spacing.s))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.onError.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.m, vertical = spacing.s),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = colors.onError
                    )
                    Spacer(modifier = Modifier.width(spacing.s))
                    Text(
                        text = message,
                        style = AppType.label,
                        color = colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Cover Art Area (Estilo Vinilo Digital) con aura derivada de la portada
        Box(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            VinylAura(
                isPlaying = state.isPlaying,
                auraColor = auraColor,
                modifier = Modifier.fillMaxSize(),
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize(0.72f)
                        .shadow(elevation = 16.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(colors.surfaceRaised)
                        .border(
                            border = BorderStroke(2.dp, colors.outline.copy(alpha = 0.3f)),
                            shape = CircleShape
                        ),
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
                                                swipeOffsetX.animateTo(widthPx, tween(250, easing = FastOutLinearInEasing))
                                                viewModel.skipPrevious()
                                                recenterAfterSkippedSwipe()
                                            } else if (swipeOffsetX.value < -150f) {
                                                // Siguiente
                                                isNext = true
                                                // Animamos hacia afuera y lanzamos el comando
                                                swipeOffsetX.animateTo(-widthPx, tween(250, easing = FastOutLinearInEasing))
                                                viewModel.skipNext()
                                                recenterAfterSkippedSwipe()
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
                                    .clip(CircleShape)
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
        var sliderValue by remember { mutableFloatStateOf(state.positionMs.toFloat()) }
        var isDragging by remember { mutableStateOf(false) }
        val duration = state.durationMs ?: 0L

        Column(modifier = Modifier.fillMaxWidth()) {
            val thumbScale by animateFloatAsState(
                targetValue = if (isDragging) 1.3f else 1f,
                animationSpec = tween(150),
                label = "seekThumbScale"
            )
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
                    thumbColor = Color.Transparent,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth(),
                track = { sliderState ->
                    val range = sliderState.valueRange
                    val fraction = if (range.endInclusive > range.start) {
                        ((sliderState.value - range.start) / (range.endInclusive - range.start))
                            .coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(colors.outline.copy(alpha = 0.15f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(gradient),
                        )
                    }
                },
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .scale(thumbScale)
                            .shadow(elevation = 6.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(auraColor),
                    )
                }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.s),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerControlButton(
                icon = Icons.Rounded.Shuffle,
                contentDescription = if (state.shuffleEnabled) "Desactivar aleatorio" else "Activar aleatorio",
                active = state.shuffleEnabled,
                onClick = { viewModel.toggleShuffle() },
                colors = colors,
            )

            PlayerControlButton(
                icon = Icons.Rounded.SkipPrevious,
                contentDescription = "Anterior",
                active = false,
                onClick = {
                    isNext = false
                    // Marcamos como no manual para que use la animación por defecto de slideOut
                    draggedSongId = null
                    viewModel.skipPrevious()
                },
                colors = colors,
                sizeDp = 56,
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(96.dp)
            ) {
                val playPulse by rememberInfiniteTransition(label = "playHalo").animateFloat(
                    initialValue = 0.7f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2_200, easing = FastOutSlowInEasing),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
                    ),
                    label = "playHaloAlpha",
                )

                // Halo difuminado suave (gradiente radial, sin recortes duros)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .graphicsLayer {
                            alpha = if (state.isPlaying) playPulse else 0.2f
                        }
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    auraColor.copy(alpha = 0.45f),
                                    auraColor.copy(alpha = 0.12f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Surface(
                    onClick = { viewModel.togglePlayPause() },
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(gradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pausa" else "Reproducir",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }

            PlayerControlButton(
                icon = Icons.Rounded.SkipNext,
                contentDescription = "Siguiente",
                active = false,
                onClick = {
                    isNext = true
                    // Marcamos como no manual
                    draggedSongId = null
                    viewModel.skipNext()
                },
                colors = colors,
                sizeDp = 56,
            )

            PlayerControlButton(
                icon = if (state.repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                contentDescription = when (state.repeatMode) {
                    RepeatMode.ONE -> "Repetir una canción"
                    RepeatMode.ALL -> "Repetir todas"
                    else -> "Repetición desactivada"
                },
                active = state.repeatMode != RepeatMode.OFF,
                onClick = { viewModel.cycleRepeatMode() },
                colors = colors,
            )
        }
        
        Spacer(modifier = Modifier.height(spacing.xl).navigationBarsPadding())
    }

    if (showEqualizerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEqualizerSheet = false },
            containerColor = colors.surfaceRaised,
            contentColor = colors.textPrimary
        ) {
            EqualizerContent(
                colors = colors,
                spacing = spacing,
                onApplyBands = { bands, preset -> viewModel.applyEqualizerBands(bands, preset) },
                onClose = { showEqualizerSheet = false }
            )
        }
    }

    if (showTrackDetailsDialog && state.currentSong != null) {
        TrackDetailsDialog(
            song = state.currentSong!!,
            onDismiss = { showTrackDetailsDialog = false },
            colors = colors,
            spacing = spacing
        )
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            current = sleepTimer,
            onSelect = { minutes ->
                viewModel.startSleepTimer(minutes * 60_000L)
                showSleepTimerDialog = false
            },
            onCancelTimer = {
                viewModel.cancelSleepTimer()
                showSleepTimerDialog = false
            },
            onDismiss = { showSleepTimerDialog = false },
            colors = colors,
            spacing = spacing
        )
    }
}

@Composable
private fun EqualizerContent(
    colors: com.auralis.player.core.ui.theme.AppColors,
    spacing: com.auralis.player.core.ui.theme.AppSpacing,
    onApplyBands: (List<Float>, String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val savedPreset = remember { EqualizerPreferences.getSavedPreset(context) }
    val savedBands = remember { EqualizerPreferences.getSavedBandValues(context) }

    var selectedPreset by remember { mutableStateOf(savedPreset) }
    val presets = listOf("Normal", "Bass Boost", "Rock", "Pop")
    val bands = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
    val bandDbValues = remember {
        mutableStateListOf(
            savedBands.getOrElse(0) { 0f },
            savedBands.getOrElse(1) { 0f },
            savedBands.getOrElse(2) { 0f },
            savedBands.getOrElse(3) { 0f },
            savedBands.getOrElse(4) { 0f }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            EqualizerPreferences.saveEqualizerSettings(context, selectedPreset, bandDbValues)
        }
    }

    // El efecto nativo lo posee el PlaybackService (unico Equalizer sobre la
    // sesion de audio). La UI solo envia los niveles y la preseleccion.
    fun applyPreset(preset: String) {
        selectedPreset = preset
        val newValues = when (preset) {
            "Bass Boost" -> listOf(6f, 4f, 0f, 0f, 0f)
            "Rock" -> listOf(5f, 2f, -1f, 3f, 5f)
            "Pop" -> listOf(-1f, 2f, 4f, 2f, -1f)
            else -> listOf(0f, 0f, 0f, 0f, 0f)
        }
        newValues.forEachIndexed { i, v -> bandDbValues[i] = v }
        onApplyBands(newValues, preset)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.l, vertical = spacing.m)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ECUALIZADOR",
                style = AppType.title.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                letterSpacing = 2.sp
            )
            TextButton(
                onClick = {
                    EqualizerPreferences.saveEqualizerSettings(context, selectedPreset, bandDbValues)
                    onClose()
                }
            ) {
                Text("LISTO", style = AppType.label, color = colors.accent)
            }
        }

        Spacer(modifier = Modifier.height(spacing.m))

        // Presets Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(spacing.s)
        ) {
            presets.forEach { preset ->
                val isSelected = preset == selectedPreset
                FilterChip(
                    selected = isSelected,
                    onClick = { applyPreset(preset) },
                    label = {
                        Text(
                            text = preset,
                            style = AppType.label.copy(fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colors.surface,
                        labelColor = colors.textSecondary,
                        selectedContainerColor = colors.accent,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(percent = 50)
                )
            }
            if (selectedPreset == "Personalizado") {
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = {
                        Text(
                            text = "Personalizado",
                            style = AppType.label.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.accentSecondary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(percent = 50)
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.l))

        // Vertical Equalizer Columns (Symmetrical SpaceEvenly Layout with Native VerticalFader)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bands.forEachIndexed { index, bandLabel ->
                val dbVal = bandDbValues[index]

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        text = "%+d dB".format(dbVal.toInt()),
                        style = AppType.timer.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = if (dbVal != 0f) colors.accent else colors.textSecondary
                    )

                    VerticalFader(
                        value = dbVal,
                        onValueChange = { newDb ->
                            bandDbValues[index] = newDb
                            selectedPreset = "Personalizado"
                            onApplyBands(bandDbValues.toList(), "Personalizado")
                        },
                        colors = colors
                    )

                    Text(
                        text = bandLabel,
                        style = AppType.label.copy(fontSize = 10.sp),
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalFader(
    value: Float,
    onValueChange: (Float) -> Unit,
    colors: com.auralis.player.core.ui.theme.AppColors,
    modifier: Modifier = Modifier
) {
    val onValueChangeState by rememberUpdatedState(onValueChange)
    val faderHeight = 130.dp
    val faderWidth = 36.dp
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .width(faderWidth)
            .height(faderHeight)
            .clipToBounds()
            .pointerInput(Unit) {
                val totalPx = with(density) { faderHeight.toPx() }
                detectVerticalDragGestures(
                    onVerticalDrag = { change, _ ->
                        change.consume()
                        val fraction = (1f - (change.position.y / totalPx)).coerceIn(0f, 1f)
                        val newDb = -15f + fraction * 30f
                        onValueChangeState(newDb.coerceIn(-15f, 15f))
                    }
                )
            }
            .pointerInput(Unit) {
                val totalPx = with(density) { faderHeight.toPx() }
                detectTapGestures { tapOffset ->
                    val fraction = (1f - (tapOffset.y / totalPx)).coerceIn(0f, 1f)
                    val newDb = -15f + fraction * 30f
                    onValueChangeState(newDb.coerceIn(-15f, 15f))
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Track Background
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(colors.outline.copy(alpha = 0.3f))
                .align(Alignment.Center)
        )

        // Active Track (from bottom)
        val normalizedFraction = ((value + 15f) / 30f).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight(normalizedFraction)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.accent)
                .align(Alignment.BottomCenter)
        )

        // Circular Thumb using IntOffset
        val thumbYOffsetPx = remember(normalizedFraction, faderHeight) {
            with(density) {
                val trackHeightPx = faderHeight.toPx()
                val thumbSizePx = 20.dp.toPx()
                val offsetFromBottomPx = normalizedFraction * (trackHeightPx - thumbSizePx)
                val offsetFromTopPx = (trackHeightPx - thumbSizePx) - offsetFromBottomPx
                offsetFromTopPx.toInt()
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset { androidx.compose.ui.unit.IntOffset(0, thumbYOffsetPx) }
                .size(20.dp)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(colors.textPrimary)
                .border(BorderStroke(2.dp, colors.accent), CircleShape)
        )
    }
}

@Composable
private fun TrackDetailsDialog(
    song: Song,
    onDismiss: () -> Unit,
    colors: com.auralis.player.core.ui.theme.AppColors,
    spacing: com.auralis.player.core.ui.theme.AppSpacing
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceRaised,
        title = {
            Text(
                text = "Detalles de la pista",
                style = AppType.title,
                color = colors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                DetailRow("Título", song.title, colors)
                DetailRow("Artista", song.artist ?: "Desconocido", colors)
                DetailRow("Álbum", song.album ?: "Sin álbum", colors)
                song.durationMs?.let {
                    DetailRow("Duración", formatDuration(it), colors)
                }
                DetailRow("Origen", song.sourceUri, colors)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ACEPTAR", style = AppType.label, color = colors.accent)
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    colors: com.auralis.player.core.ui.theme.AppColors
) {
    Column {
        Text(
            text = label.uppercase(),
            style = AppType.label.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
            color = colors.accentSecondary
        )
        Text(
            text = value,
            style = AppType.body.copy(fontSize = 12.sp),
            color = colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AudioEqualizerVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = ReproductorThemeTokens.colors.accent,
    barMaxHeightDp: Int = 12,
    barWidthDp: Int = 3
) {
    val barCount = 4
    val transition = rememberInfiniteTransition(label = "EqualizerTransition")

    val barHeights = List(barCount) { index ->
        val duration = 350 + (index * 120)
        if (isPlaying) {
            val height by transition.animateFloat(
                initialValue = 3f,
                targetValue = (barMaxHeightDp * 0.5f) + (index % 3) * (barMaxHeightDp * 0.25f),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                ),
                label = "BarHeight_$index"
            )
            height
        } else {
            3f
        }
    }

    Row(
        modifier = modifier.height(barMaxHeightDp.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        barHeights.forEach { heightDp ->
            Box(
                modifier = Modifier
                    .width(barWidthDp.dp)
                    .height(heightDp.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun PlayerControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    active: Boolean,
    onClick: () -> Unit,
    colors: com.auralis.player.core.ui.theme.AppColors,
    sizeDp: Int = 48,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = tween(120),
        label = "controlButtonScale"
    )
    IconButton(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier
            .size(sizeDp.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(if (active) colors.accent.copy(alpha = 0.14f) else Color.Transparent),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (active) colors.accent else colors.textSecondary,
            modifier = Modifier.size((sizeDp * 0.46f).dp),
        )
    }
}

private val SLEEP_TIMER_OPTIONS_MINUTES = listOf(5, 10, 15, 30, 45, 60)

private fun sleepTimerLabel(state: SleepTimerState): String = when (state) {
    SleepTimerState.Off -> "Temporizador"
    is SleepTimerState.Running -> {
        val totalSeconds = (state.remainingMs / 1000).coerceAtLeast(0)
        "Temporizador · %d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }
}

@Composable
private fun SleepTimerDialog(
    current: SleepTimerState,
    onSelect: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit,
    colors: com.auralis.player.core.ui.theme.AppColors,
    spacing: com.auralis.player.core.ui.theme.AppSpacing,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceRaised,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textSecondary,
        title = { Text("Temporizador de sueño", style = AppType.title) },
        text = {
            Column {
                SLEEP_TIMER_OPTIONS_MINUTES.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.s)
                    ) {
                        row.forEach { minutes ->
                            OutlinedButton(
                                onClick = { onSelect(minutes) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colors.accent
                                )
                            ) {
                                Text("$minutes min", style = AppType.label)
                            }
                        }
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(spacing.s))
                }
            }
        },
        confirmButton = {
            if (current is SleepTimerState.Running) {
                TextButton(onClick = onCancelTimer) {
                    Text("Desactivar", color = colors.onError, style = AppType.label)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = colors.textSecondary, style = AppType.label)
            }
        }
    )
}

