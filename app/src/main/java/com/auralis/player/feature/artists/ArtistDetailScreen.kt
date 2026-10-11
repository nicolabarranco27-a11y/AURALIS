package com.auralis.player.feature.artists

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.player.core.common.format.formatDuration
import com.auralis.player.core.ui.components.AuralisArtwork
import com.auralis.player.core.ui.components.QueueFeedbackOverlay
import com.auralis.player.core.ui.components.MiniPlayerBar
import com.auralis.player.core.ui.components.LoadingState
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.Album
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.Song
import kotlinx.coroutines.launch

@Composable
fun ArtistDetailScreen(
    onBackClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onAlbumClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArtistDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing
    
    var showQueueFeedback by remember { mutableStateOf(false) }

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
                        ArtistDetailContent(
                            state = state,
                            onBackClick = onBackClick,
                            onSongClick = { viewModel.onSongClick(it) },
                            onAddToQueue = {
                                viewModel.addToQueue(it)
                                showQueueFeedback = true
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onPlayerClick = onPlayerClick,
                            onAlbumClick = onAlbumClick,
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
private fun ArtistDetailContent(
    state: ArtistDetailUiState,
    onBackClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onPlayPauseClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onAlbumClick: (String) -> Unit,
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
                ArtistDetailHeader(
                    name = state.artistName,
                    onBackClick = onBackClick,
                    colors = colors,
                    spacing = spacing
                )
            }

            if (state.albums.isNotEmpty()) {
                item {
                    Text(
                        text = "ÁLBUMES",
                        style = AppType.label.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(horizontal = spacing.l, vertical = spacing.m)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = spacing.l),
                        horizontalArrangement = Arrangement.spacedBy(spacing.m),
                        modifier = Modifier.padding(bottom = spacing.l)
                    ) {
                        items(state.albums, key = { it.id.value }) { album ->
                            ArtistAlbumItem(
                                album = album,
                                onClick = { onAlbumClick(album.id.value) },
                                colors = colors,
                                spacing = spacing
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "CANCIONES",
                    style = AppType.label.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = spacing.l, vertical = spacing.m)
                )
            }

            items(state.songs, key = { it.id.value }) { song ->
                val isCurrent = state.playbackState.currentSong?.id == song.id
                SongItem(
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

@Composable
private fun ArtistDetailHeader(
    name: String,
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

        Text(
            text = name,
            style = AppType.display.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(spacing.l))
    }
}

@Composable
private fun ArtistAlbumItem(
    album: Album,
    onClick: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        AuralisArtwork(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp)),
            artworkReference = album.coverReference
        )
        Spacer(modifier = Modifier.height(spacing.xs))
        Text(
            text = album.title,
            style = AppType.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SongItem(
    song: Song,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onSwipeRight: () -> Unit,
    onToggleFavorite: () -> Unit,
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
            .background(if (offsetX > 0) colors.accent.copy(alpha = 0.2f * (offsetX / 150f).coerceAtMost(1f)) else Color.Transparent)
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
                song.album?.let {
                    Text(
                        text = it.uppercase(),
                        style = AppType.label.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(spacing.s))

            song.durationMs?.let { duration ->
                Text(
                    text = formatDuration(duration),
                    style = AppType.timer,
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }

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
        }
    }
}

