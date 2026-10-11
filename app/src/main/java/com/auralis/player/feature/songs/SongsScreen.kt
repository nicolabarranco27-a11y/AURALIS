package com.auralis.player.feature.songs

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
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
import com.auralis.player.core.ui.components.MiniPlayerBar
import com.auralis.player.core.ui.components.QueueFeedbackOverlay
import com.auralis.player.core.ui.components.SongRow
import com.auralis.player.core.ui.components.LoadingState
import com.auralis.player.core.ui.components.EmptyState
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.Song
import kotlinx.coroutines.launch

@Composable
fun SongsScreen(
    onMenuClick: () -> Unit,
    onPlayerClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SongsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing
    
    var showQueueFeedback by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            SongsHeader(
                count = state.songs.size,
                onMenuClick = onMenuClick,
                colors = colors,
                spacing = spacing
            )
        }
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
                state.songs.isEmpty() -> {
                    EmptyState("No hay canciones disponibles", colors = colors)
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        SongsContent(
                            state = state,
                            onSongClick = { viewModel.onSongClick(it) },
                            onAddToQueue = { 
                                viewModel.addToQueue(it)
                                showQueueFeedback = true
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
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
}

@Composable
private fun SongsHeader(
    count: Int,
    onMenuClick: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = spacing.l, vertical = spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Abrir menú",
                tint = colors.textSecondary
            )
        }
        
        Spacer(modifier = Modifier.width(spacing.s))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "CANCIONES",
                style = AppType.display.copy(fontSize = 24.sp),
                color = colors.textPrimary,
                fontWeight = FontWeight.Light,
                letterSpacing = 4.sp
            )
            Text(
                text = "$count canciones en total",
                style = AppType.label.copy(fontSize = 10.sp),
                color = colors.textSecondary.copy(alpha = 0.8f),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun SongsContent(
    state: SongsUiState,
    onSongClick: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
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
            items(state.songs, key = { it.id.value }) { song ->
                val isCurrent = state.playbackState.currentSong?.id == song.id
                SongRow(
                    song = song,
                    isCurrent = isCurrent,
                    onClick = { onSongClick(song) },
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

