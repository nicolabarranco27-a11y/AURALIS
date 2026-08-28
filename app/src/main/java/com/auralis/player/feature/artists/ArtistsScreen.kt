package com.auralis.player.feature.artists

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
import com.auralis.player.domain.model.Artist

@Composable
fun ArtistsScreen(
    onMenuClick: () -> Unit,
    onArtistClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArtistsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            ArtistsHeader(
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
                state.artists.isEmpty() -> {
                    EmptyArtistsState()
                }
                else -> {
                    ArtistsList(
                        artists = state.artists,
                        onArtistClick = onArtistClick,
                        colors = colors,
                        spacing = spacing
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistsHeader(
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
            Text(
                text = "☰",
                style = AppType.title.copy(fontSize = 24.sp),
                color = colors.textSecondary
            )
        }
        
        Spacer(modifier = Modifier.width(spacing.s))

        Text(
            text = "ARTISTAS",
            style = AppType.display.copy(fontSize = 24.sp),
            color = colors.textPrimary,
            fontWeight = FontWeight.Light,
            letterSpacing = 4.sp
        )
    }
}

@Composable
private fun ArtistsList(
    artists: List<Artist>,
    onArtistClick: (String) -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(artists, key = { it.id.value }) { artist ->
            ArtistItem(
                artist = artist,
                onClick = { onArtistClick(artist.name) },
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
private fun ArtistItem(
    artist: Artist,
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
        // Artist Icon Placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(colors.surfaceRaised),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👤",
                style = AppType.title.copy(fontSize = 20.sp),
                color = colors.textSecondary.copy(alpha = 0.4f)
            )
        }

        Spacer(modifier = Modifier.width(spacing.m))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                style = AppType.body.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${artist.songCount} canciones • ${artist.albumCount} álbumes",
                style = AppType.label.copy(fontSize = 10.sp),
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LoadingState(colors: AppColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = colors.accent, strokeWidth = 2.dp)
    }
}

@Composable
private fun EmptyArtistsState() {
    val colors = ReproductorThemeTokens.colors
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No se encontraron artistas",
            style = AppType.body,
            color = colors.textSecondary
        )
    }
}
