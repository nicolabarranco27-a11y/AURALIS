// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.feature.playlists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.player.core.common.format.formatSongCount
import com.auralis.player.core.common.format.formatTotalDuration
import com.auralis.player.core.ui.components.AuralisArtwork
import com.auralis.player.core.ui.components.ConfirmDeleteDialog
import com.auralis.player.core.ui.components.EditPlaylistDialog
import com.auralis.player.core.ui.components.MiniPlayerBar
import com.auralis.player.core.ui.components.QueueFeedbackOverlay
import com.auralis.player.core.ui.components.SongAction
import com.auralis.player.core.ui.components.SongActionsSheet
import com.auralis.player.core.ui.components.SongListItem
import com.auralis.player.core.ui.components.songActions
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId

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
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var actionSong by remember { mutableStateOf<Song?>(null) }
    var actionIndex by remember { mutableIntStateOf(-1) }

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
                            onEditClick = { showEditDialog = true },
                            onDeleteClick = { showDeleteDialog = true },
                            onAddSongsClick = { onAddSongsClick(state.playlist!!.id.value) },
                            onPlayAll = { viewModel.playAll(shuffle = false) },
                            onShufflePlay = { viewModel.playAll(shuffle = true) },
                            onSongClick = { viewModel.onSongClick(it) },
                            onAddToQueue = {
                                viewModel.addToQueue(it)
                                showQueueFeedback = true
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onLongPress = { song, index ->
                                actionSong = song
                                actionIndex = index
                            },
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

    if (showEditDialog && state.playlist != null) {
        EditPlaylistDialog(
            currentName = state.playlist!!.name,
            currentDescription = state.playlist!!.description,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, description ->
                viewModel.updateDetails(name, description)
                showEditDialog = false
            },
            colors = colors,
            spacing = spacing
        )
    }

    if (showDeleteDialog && state.playlist != null) {
        ConfirmDeleteDialog(
            title = "Eliminar playlist",
            message = "¿Seguro que quieres eliminar \"${state.playlist!!.name}\"? Esta acción no se puede deshacer.",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deletePlaylist()
                showDeleteDialog = false
                onBackClick()
            },
            colors = colors
        )
    }

    actionSong?.let { song ->
        val songCount = state.songs.size
        SongActionsSheet(
            song = song,
            actions = songActions(
                song = song,
                onPlay = { viewModel.onSongClick(song) },
                onAddToQueue = {
                    viewModel.addToQueue(song)
                    showQueueFeedback = true
                },
                onToggleFavorite = { viewModel.toggleFavorite(song) },
                onHide = { viewModel.hideSong(song) },
                extra = buildList {
                    add(
                        SongAction(
                            icon = Icons.Rounded.ArrowUpward,
                            label = "Mover arriba",
                            enabled = actionIndex > 0,
                            onClick = { viewModel.moveSong(song.id, actionIndex - 1) },
                        )
                    )
                    add(
                        SongAction(
                            icon = Icons.Rounded.ArrowDownward,
                            label = "Mover abajo",
                            enabled = actionIndex in 0 until songCount - 1,
                            onClick = { viewModel.moveSong(song.id, actionIndex + 1) },
                        )
                    )
                    add(
                        SongAction(
                            icon = Icons.Rounded.Remove,
                            label = "Quitar de esta playlist",
                            destructive = true,
                            onClick = { viewModel.removeSong(song.id) },
                        )
                    )
                },
            ),
            onDismiss = { actionSong = null },
        )
    }
}

@Composable
private fun PlaylistDetailContent(
    state: PlaylistDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddSongsClick: () -> Unit,
    onPlayAll: () -> Unit,
    onShufflePlay: () -> Unit,
    onSongClick: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onLongPress: (Song, Int) -> Unit,
    onRemoveSong: (SongId) -> Unit,
    onPlayPauseClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    val totalDuration = remember(state.songs) { state.songs.sumOf { it.durationMs ?: 0L } }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            item {
                PlaylistDetailHeader(
                    name = state.playlist?.name ?: "",
                    description = state.playlist?.description,
                    coverReference = state.songs.firstOrNull()?.coverReference,
                    songCount = state.songs.size,
                    totalDurationMs = totalDuration,
                    onBackClick = onBackClick,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    onAddSongsClick = onAddSongsClick,
                    onPlayAll = onPlayAll,
                    onShufflePlay = onShufflePlay,
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

            itemsIndexed(state.songs, key = { _, song -> song.id.value }) { index, song ->
                val isCurrent = state.playbackState.currentSong?.id == song.id
                SongListItem(
                    song = song,
                    isCurrent = isCurrent,
                    onClick = { onSongClick(song) },
                    onLongClick = { onLongPress(song, index) },
                    onSwipeRight = { onAddToQueue(song) },
                    onToggleFavorite = { onToggleFavorite(song) },
                    trailingContent = {
                        IconButton(onClick = { onRemoveSong(song.id) }) {
                            Icon(
                                imageVector = Icons.Rounded.Remove,
                                contentDescription = "Quitar de la playlist",
                                tint = colors.onError.copy(alpha = 0.6f)
                            )
                        }
                    },
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
    description: String?,
    coverReference: String?,
    songCount: Int,
    totalDurationMs: Long,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddSongsClick: () -> Unit,
    onPlayAll: () -> Unit,
    onShufflePlay: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.l, vertical = spacing.m)
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

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Opciones",
                        tint = colors.textPrimary
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = colors.surfaceRaised
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar", style = AppType.body, color = colors.textPrimary) },
                        onClick = {
                            menuExpanded = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", style = AppType.body, color = colors.onError) },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.m))

        Row(verticalAlignment = Alignment.CenterVertically) {
            AuralisArtwork(
                modifier = Modifier
                    .size(96.dp)
                    .clip(ReproductorThemeTokens.shapes.medium),
                artworkReference = coverReference
            )
            Spacer(modifier = Modifier.width(spacing.m))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = AppType.display.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = buildString {
                        append(formatSongCount(songCount))
                        if (totalDurationMs > 0) {
                            append(" · ")
                            append(formatTotalDuration(totalDurationMs))
                        }
                    },
                    style = AppType.label.copy(fontSize = 12.sp),
                    color = colors.textSecondary
                )
                if (!description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(spacing.xs))
                    Text(
                        text = description,
                        style = AppType.body.copy(fontSize = 13.sp),
                        color = colors.textSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.l))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPlayAll,
                enabled = songCount > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                shape = ReproductorThemeTokens.shapes.medium,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(spacing.xs))
                Text("REPRODUCIR", style = AppType.label)
            }

            FilledIconButton(
                onClick = onShufflePlay,
                enabled = songCount > 0,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = colors.surfaceRaised,
                    contentColor = colors.accent
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Aleatorio"
                )
            }

            FilledIconButton(
                onClick = onAddSongsClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = colors.surfaceRaised,
                    contentColor = colors.accent
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Añadir canciones"
                )
            }
        }
    }
}