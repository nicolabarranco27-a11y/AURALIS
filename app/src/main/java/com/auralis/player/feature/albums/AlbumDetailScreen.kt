// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.feature.albums

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.player.core.common.format.formatDuration
import com.auralis.player.core.ui.components.AuralisArtwork
import com.auralis.player.core.ui.components.QueueFeedbackOverlay
import com.auralis.player.core.ui.components.SongActionsSheet
import com.auralis.player.core.ui.components.SongListItem
import com.auralis.player.core.ui.components.songActions
import com.auralis.player.core.ui.components.MiniPlayerBar
import com.auralis.player.core.ui.components.LoadingState
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.Song
import kotlinx.coroutines.launch

@Composable
fun AlbumDetailScreen(
    onBackClick: () -> Unit,
    onPlayerClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing
    
    var showQueueFeedback by remember { mutableStateOf(false) }
    var actionSong by remember { mutableStateOf<Song?>(null) }

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
                    LoadingState(colors = colors)
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AlbumDetailContent(
                            state = state,
                            onBackClick = onBackClick,
                            onSongClick = { viewModel.onSongClick(it) },
                            onAddToQueue = {
                                viewModel.addToQueue(it)
                                showQueueFeedback = true
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onLongPress = { actionSong = it },
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

    actionSong?.let { song ->
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
            ),
            onDismiss = { actionSong = null },
        )
    }
}

@Composable
private fun AlbumDetailContent(
    state: AlbumDetailUiState,
    onBackClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onLongPress: (Song) -> Unit,
    onPlayPauseClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    val coverReference = remember(state.songs) { state.songs.firstOrNull { !it.coverReference.isNullOrBlank() }?.coverReference }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            item {
                AlbumHeader(
                    title = state.albumTitle,
                    artist = state.artist,
                    coverReference = coverReference,
                    onBackClick = onBackClick,
                    colors = colors,
                    spacing = spacing
                )
            }

            items(state.songs, key = { it.id.value }) { song ->
                val isCurrent = state.playbackState.currentSong?.id == song.id
                SongListItem(
                    song = song,
                    isCurrent = isCurrent,
                    onClick = { onSongClick(song) },
                    onLongClick = { onLongPress(song) },
                    onSwipeRight = { onAddToQueue(song) },
                    onToggleFavorite = { onToggleFavorite(song) },
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
private fun AlbumHeader(
    title: String,
    artist: String?,
    coverReference: String?,
    onBackClick: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing.l)
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

        Spacer(modifier = Modifier.height(spacing.m))

        Row(verticalAlignment = Alignment.Bottom) {
            // Mini Cover
            AuralisArtwork(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                artworkReference = coverReference
            )

            Spacer(modifier = Modifier.width(spacing.l))

            Column {
                Text(
                    text = title,
                    style = AppType.title.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = (artist ?: "Artista desconocido").uppercase(),
                    style = AppType.label.copy(letterSpacing = 2.sp),
                    color = colors.accentSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(spacing.xl))
    }
}

