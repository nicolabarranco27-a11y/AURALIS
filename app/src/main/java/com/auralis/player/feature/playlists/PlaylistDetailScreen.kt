package com.auralis.player.feature.playlists

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import kotlinx.coroutines.launch

@Composable
fun PlaylistDetailScreen(
    onBackClick: () -> Unit,
    onAddSongsClick: (String) -> Unit,
    onPlayerClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing

    var showRenameDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.accent)
                    }
                }
                state.playlist == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Playlist no encontrada", color = colors.textSecondary)
                    }
                }
                else -> {
                    PlaylistDetailContent(
                        state = state,
                        onBackClick = onBackClick,
                        onRenameClick = { showRenameDialog = true },
                        onAddSongsClick = { onAddSongsClick(state.playlist!!.id.value) },
                        onSongClick = { viewModel.onSongClick(it) },
                        onRemoveSong = { viewModel.removeSong(it) },
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onPlayerClick = onPlayerClick,
                        onSeek = { viewModel.seekTo(it) },
                        onSkipNext = { viewModel.skipNext() },
                        onSkipPrevious = { viewModel.skipPrevious() },
                        colors = colors,
                        spacing = spacing
                    )
                }
            }
        }
    }

    if (showRenameDialog && state.playlist != null) {
        RenamePlaylistDialog(
            currentName = state.playlist!!.name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { 
                viewModel.renamePlaylist(it)
                showRenameDialog = false
            },
            colors = colors
        )
    }
}

@Composable
private fun PlaylistDetailContent(
    state: PlaylistDetailUiState,
    onBackClick: () -> Unit,
    onRenameClick: () -> Unit,
    onAddSongsClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onRemoveSong: (SongId) -> Unit,
    onPlayPauseClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            item {
                PlaylistDetailHeader(
                    name = state.playlist?.name ?: "",
                    onBackClick = onBackClick,
                    onRenameClick = onRenameClick,
                    onAddSongsClick = onAddSongsClick,
                    colors = colors,
                    spacing = spacing
                )
            }

            if (state.songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Esta playlist está vacía",
                            style = AppType.body,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            items(state.songs, key = { it.id.value }) { song ->
                val isCurrent = state.playbackState.currentSong?.id == song.id
                SongItem(
                    song = song,
                    isCurrent = isCurrent,
                    onClick = { onSongClick(song) },
                    onRemove = { onRemoveSong(song.id) },
                    colors = colors,
                    spacing = spacing
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = spacing.l),
                    thickness = 0.5.dp,
                    color = colors.outline.copy(alpha = 0.5f)
                )
            }
        }

        if (state.playbackState.currentSong != null) {
            PlaybackBar(
                state = state.playbackState,
                onPlayPauseClick = onPlayPauseClick,
                onPlayerClick = onPlayerClick,
                onSeek = onSeek,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                colors = colors,
                spacing = spacing,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(spacing.m)
            )
        }
    }
}

@Composable
private fun PlaylistDetailHeader(
    name: String,
    onBackClick: () -> Unit,
    onRenameClick: () -> Unit,
    onAddSongsClick: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing.l)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                Text("←", style = AppType.title, color = colors.textPrimary)
            }
            
            TextButton(onClick = onRenameClick) {
                Text("RENOMBRAR", style = AppType.label, color = colors.accent)
            }
        }

        Spacer(modifier = Modifier.height(spacing.m))

        Text(
            text = name,
            style = AppType.display.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(spacing.l))
        
        Button(
            onClick = onAddSongsClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.surfaceRaised,
                contentColor = colors.accent
            ),
            shape = ReproductorThemeTokens.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("AÑADIR CANCIONES", style = AppType.label)
        }
    }
}

@Composable
private fun SongItem(
    song: Song,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isCurrent) colors.surfaceRaised.copy(alpha = 0.5f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.l, vertical = spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(ReproductorThemeTokens.shapes.small)
                .then(
                    if (isCurrent) Modifier.background(ReproductorThemeTokens.primaryGradient)
                    else Modifier.background(colors.surfaceRaised)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isCurrent) "▶" else "♪",
                color = if (isCurrent) Color.White else colors.textSecondary.copy(alpha = 0.4f),
                style = AppType.title.copy(fontSize = 16.sp)
            )
        }

        Spacer(modifier = Modifier.width(spacing.m))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = AppType.body.copy(fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium),
                color = if (isCurrent) colors.accent else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = (song.artist ?: "Artista desconocido").uppercase(),
                style = AppType.label.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(spacing.s))
        
        IconButton(onClick = onRemove) {
            Text("−", style = AppType.title, color = colors.onError.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun RenamePlaylistDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    colors: AppColors
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceRaised,
        title = { Text("Renombrar Playlist", style = AppType.title, color = colors.textPrimary) },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = colors.accent,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                textStyle = AppType.body,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text("GUARDAR", style = AppType.label, color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", style = AppType.label, color = colors.textSecondary)
            }
        }
    )
}

@Composable
private fun PlaybackBar(
    state: PlaybackState,
    onPlayPauseClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember(state.positionMs) { mutableFloatStateOf(state.positionMs.toFloat()) }
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
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(ReproductorThemeTokens.shapes.small)
                        .background(colors.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "♪", color = colors.accent)
                }
                
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
                    Text(
                        text = if (state.isPlaying) "‖" else "▶",
                        style = AppType.title,
                        color = colors.textPrimary
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

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
