package com.auralis.player.feature.songs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.player.core.ui.components.LoadingState
import com.auralis.player.core.ui.components.MiniPlayerBar
import com.auralis.player.core.ui.components.QueueFeedbackOverlay
import com.auralis.player.core.ui.components.SongRow
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.Song
import com.auralis.player.feature.folders.MusicFoldersSheet
import com.auralis.player.feature.folders.rememberFolderPickerLauncher

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
    var showFolderSheet by remember { mutableStateOf(false) }
    val openFolderPicker = rememberFolderPickerLauncher { uri -> viewModel.addFolder(uri) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            SongsHeader(
                count = state.songs.size,
                onMenuClick = onMenuClick,
                onFoldersClick = { showFolderSheet = true },
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
                    NoSongsState(
                        onClick = { showFolderSheet = true },
                        colors = colors,
                        spacing = spacing
                    )
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

    if (showFolderSheet) {
        MusicFoldersSheet(
            folders = state.selectedFolders,
            onAddFolder = { openFolderPicker() },
            onRemoveFolder = { viewModel.removeFolder(it) },
            onDismiss = { showFolderSheet = false }
        )
    }
}

@Composable
private fun SongsHeader(
    count: Int,
    onMenuClick: () -> Unit,
    onFoldersClick: () -> Unit,
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

        IconButton(onClick = onFoldersClick) {
            Icon(
                imageVector = Icons.Rounded.FolderOpen,
                contentDescription = "Carpetas de música",
                tint = colors.textSecondary
            )
        }
    }
}

@Composable
private fun NoSongsState(
    onClick: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sin canciones",
            style = AppType.title,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(spacing.s))
        Text(
            text = "No encontramos canciones. Agrega las carpetas donde tienes tu música.",
            style = AppType.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = spacing.m)
        )
        Spacer(modifier = Modifier.height(spacing.xl))
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(percent = 50),
            color = colors.accent
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.FolderOpen,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(spacing.s))
                Text(
                    text = "Agregar carpetas",
                    style = AppType.label.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
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
