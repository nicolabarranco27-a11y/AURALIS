package com.auralis.player.feature.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId

@Composable
fun AddToPlaylistScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddToPlaylistViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            AddToPlaylistHeader(
                onBackClick = onBackClick,
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.accent)
                    }
                }
                state.allSongs.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay canciones disponibles", color = colors.textSecondary)
                    }
                }
                else -> {
                    SongsSelectorList(
                        songs = state.allSongs,
                        selectedIds = state.playlistSongsIds,
                        onToggle = { viewModel.toggleSongInPlaylist(it) },
                        colors = colors,
                        spacing = spacing
                    )
                }
            }
        }
    }
}

@Composable
private fun AddToPlaylistHeader(
    onBackClick: () -> Unit,
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
        IconButton(onClick = onBackClick) {
            Text("←", style = AppType.title, color = colors.textPrimary)
        }
        
        Spacer(modifier = Modifier.width(spacing.s))

        Text(
            text = "AÑADIR A PLAYLIST",
            style = AppType.display.copy(fontSize = 20.sp),
            color = colors.textPrimary,
            fontWeight = FontWeight.Light,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun SongsSelectorList(
    songs: List<Song>,
    selectedIds: Set<String>,
    onToggle: (SongId) -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(songs, key = { it.id.value }) { song ->
            val isSelected = selectedIds.contains(song.id.value)
            SelectableSongItem(
                song = song,
                isSelected = isSelected,
                onClick = { onToggle(song.id) },
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
}

@Composable
private fun SelectableSongItem(
    song: Song,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.l, vertical = spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(ReproductorThemeTokens.shapes.small)
                .background(if (isSelected) colors.accent.copy(alpha = 0.2f) else colors.surfaceRaised),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Text("✓", color = colors.accent, style = AppType.title)
            } else {
                Text("♪", color = colors.textSecondary.copy(alpha = 0.4f), style = AppType.title)
            }
        }

        Spacer(modifier = Modifier.width(spacing.m))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = AppType.body.copy(fontWeight = FontWeight.Medium),
                color = if (isSelected) colors.accent else colors.textPrimary,
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
        
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(
                checkedColor = colors.accent,
                uncheckedColor = colors.outline,
                checkmarkColor = Color.White
            )
        )
    }
}
