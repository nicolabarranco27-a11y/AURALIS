package com.auralis.player.feature.hidden

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.player.core.ui.components.LoadingState
import com.auralis.player.core.ui.components.MiniPlayerBar
import com.auralis.player.core.ui.components.SongListItem
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.Song

@Composable
fun HiddenSongsScreen(
    onBackClick: () -> Unit,
    onPlayerClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HiddenSongsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing

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
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Volver",
                        tint = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.width(spacing.s))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CANCIONES OCULTAS",
                        style = AppType.display.copy(fontSize = 22.sp),
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "${state.songs.size} ocultas de tu biblioteca",
                        style = AppType.label.copy(fontSize = 10.sp),
                        color = colors.textSecondary.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                }

                if (state.songs.isNotEmpty()) {
                    TextButton(onClick = { viewModel.restoreAll() }) {
                        Text("RESTAURAR TODAS", style = AppType.label, color = colors.accent)
                    }
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
                    LoadingState(colors = colors)
                }
                state.songs.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No hay canciones ocultas",
                                style = AppType.body,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.padding(spacing.s))
                            Text(
                                text = "Mantén presionada una canción para ocultarla.",
                                style = AppType.label.copy(fontSize = 11.sp),
                                color = colors.textSecondary.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 140.dp)
                        ) {
                            items(state.songs, key = { it.id.value }) { song ->
                                SongListItem(
                                    song = song,
                                    isCurrent = state.playbackState.currentSong?.id == song.id,
                                    onClick = { viewModel.onSongClick(song) },
                                    onLongClick = {},
                                    onSwipeRight = { viewModel.addToQueue(song) },
                                    onToggleFavorite = {},
                                    showFavorite = false,
                                    trailingContent = {
                                        IconButton(onClick = { viewModel.restoreSong(song) }) {
                                            Icon(
                                                imageVector = Icons.Rounded.Restore,
                                                contentDescription = "Restaurar a la biblioteca",
                                                tint = colors.accent
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
                    }
                }
            }
        }
    }
}