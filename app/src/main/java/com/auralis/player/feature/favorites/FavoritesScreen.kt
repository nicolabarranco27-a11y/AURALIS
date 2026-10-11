package com.auralis.player.feature.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.player.core.ui.components.MiniPlayerBar
import com.auralis.player.core.ui.components.QueueFeedbackOverlay
import com.auralis.player.core.ui.components.SongRow
import com.auralis.player.core.ui.theme.ReproductorThemeTokens

@Composable
fun FavoritesScreen(
    onMenuClick: () -> Unit,
    onPlayerClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing

    var showQueueFeedback by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
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
                        text = "FAVORITOS",
                        style = com.auralis.player.core.ui.theme.AppType.display.copy(fontSize = 24.sp),
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 4.sp
                    )
                    Text(
                        text = "${state.songs.size} canciones favoritas",
                        style = com.auralis.player.core.ui.theme.AppType.label.copy(fontSize = 10.sp),
                        color = colors.textSecondary.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.accent, strokeWidth = 2.dp)
                    }
                }
                state.songs.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Todavía no tienes canciones favoritas",
                            style = com.auralis.player.core.ui.theme.AppType.body,
                            color = colors.textSecondary
                        )
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 140.dp)
                        ) {
                            items(state.songs, key = { it.id.value }) { song ->
                                SongRow(
                                    song = song,
                                    isCurrent = state.playbackState.currentSong?.id == song.id,
                                    onClick = { viewModel.onSongClick(song) },
                                    onSwipeRight = {
                                        viewModel.addToQueue(song)
                                        showQueueFeedback = true
                                    },
                                    onToggleFavorite = { viewModel.toggleFavorite(song) },
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
                                onPlayPauseClick = { viewModel.togglePlayPause() },
                                onPlayerClick = onPlayerClick,
                                onSeek = { viewModel.seekTo(it) },
                                onSkipNext = { viewModel.skipNext() },
                                onSkipPrevious = { viewModel.skipPrevious() },
                                colors = colors,
                                spacing = spacing,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(spacing.m)
                            )
                        }

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
