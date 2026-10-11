package com.auralis.player.feature.playlists

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Remove
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
import com.auralis.player.core.common.format.formatDuration
import com.auralis.player.core.ui.components.AuralisArtwork
import com.auralis.player.core.ui.components.QueueFeedbackOverlay
import com.auralis.player.core.ui.components.MiniPlayerBar
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

    var showQueueFeedback by remember { mutableStateOf(false) }
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
                    Box(modifier = Modifier.fillMaxSize()) {
                        PlaylistDetailContent(
                            state = state,
                            onBackClick = onBackClick,
                            onRenameClick = { showRenameDialog = true },
                            onAddSongsClick = { onAddSongsClick(state.playlist!!.id.value) },
                            onSongClick = { viewModel.onSongClick(it) },
                            onAddToQueue = {
                                viewModel.addToQueue(it)
                                showQueueFeedback = true
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onRemoveSong = { viewModel.removeSong(it) },
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onPlayerClick = onPlayerClick,
                            onSeek = { viewModel.seekTo(it) },
                            onSkipNext = { viewModel.skipNext() },
                            onSkipPrevious = { viewModel.skipPrevious() },
                            colors = colors,
                            spacing = spacing
                        )
                        
                        QueueFeedbackOverlay(
                            visible = showQueueFeedback,
                            onDismiss = { showQueueFeedback = false }
                        )
                    }
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
    onAddToQueue: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
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
                    onSwipeRight = { onAddToQueue(song) },
                    onToggleFavorite = { onToggleFavorite(song) },
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
            MiniPlayerBar(
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
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Volver",
                    tint = colors.textPrimary
                )
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
    onSwipeRight: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRemove: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(0f, 300f)
                    },
                    onDragEnd = {
                        if (offsetX > 150f) {
                            onSwipeRight()
                        }
                        scope.launch {
                            Animatable(offsetX).animateTo(
                                targetValue = 0f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            ) {
                                offsetX = value
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            Animatable(offsetX).animateTo(0f) {
                                offsetX = value
                            }
                        }
                    }
                )
            }
            .background(if (offsetX > 0) colors.accent.copy(alpha = 0.2f * (offsetX / 100f).coerceAtMost(1f)) else Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = offsetX }
                .background(if (isCurrent) colors.surfaceRaised.copy(alpha = 0.5f) else colors.background)
                .clickable(enabled = offsetX == 0f, onClick = onClick)
                .padding(horizontal = spacing.l, vertical = spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuralisArtwork(
                modifier = Modifier
                    .size(40.dp)
                    .clip(ReproductorThemeTokens.shapes.small),
                artworkReference = song.coverReference
            )

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

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (song.isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                    tint = if (song.isFavorite) Color(0xFFE53935) else colors.textSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = "Quitar de la playlist",
                    tint = colors.onError.copy(alpha = 0.6f)
                )
            }
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

