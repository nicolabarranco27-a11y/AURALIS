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
                    ArtistDetailContent(
                        state = state,
                        onBackClick = onBackClick,
                        onSongClick = { viewModel.onSongClick(it) },
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onPlayerClick = onPlayerClick,
                        onAlbumClick = onAlbumClick,
                        onSeek = { viewModel.seekTo(it) },
                        onSkipNext = { viewModel.skipNext() },
                        onSkipPrevious = { viewModel.skipPrevious() },
                        colors = colors,
                        spacing = spacing
                    )
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
            PlaybackBar(
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
            Text("←", style = AppType.title, color = colors.textPrimary)
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
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surfaceRaised),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▢",
                style = AppType.display.copy(fontSize = 40.sp),
                color = colors.textSecondary.copy(alpha = 0.2f)
            )
        }
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
    colors: AppColors,
    spacing: AppSpacing
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isCurrent) colors.surfaceRaised.copy(alpha = 0.5f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.l, vertical = spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(ReproductorThemeTokens.shapes.small)
                .then(
                    if (isCurrent) Modifier.background(ReproductorThemeTokens.primaryGradient)
                    else Modifier.background(colors.surfaceRaised)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isCurrent) "▶" else "♪",
                color = if (isCurrent) Color.White else colors.textSecondary.copy(alpha = 0.4f),
                style = AppType.title.copy(fontSize = 16.sp)
            )
        }

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
    }
}

@Composable
private fun PlaybackBar(
    state: PlaybackState,
    onPlayPauseClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember(state.positionMs) { mutableFloatStateOf(state.positionMs.toFloat()) }
    var isDragging by remember { mutableStateOf(false) }
    val swipeOffsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Surface(
        onClick = onPlayerClick,
        modifier = modifier.fillMaxWidth(),
        shape = ReproductorThemeTokens.shapes.medium,
        color = colors.surfaceRaised,
        tonalElevation = 8.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { },
                            onHorizontalDrag = { _, dragAmount ->
                                scope.launch {
                                    swipeOffsetX.snapTo(swipeOffsetX.value + dragAmount * 0.8f)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    if (swipeOffsetX.value > 150f) {
                                        onSkipPrevious()
                                        launch {
                                            swipeOffsetX.animateTo(800f, tween(150, easing = LinearOutSlowInEasing))
                                            swipeOffsetX.snapTo(-800f)
                                            swipeOffsetX.animateTo(0f, spring(stiffness = Spring.StiffnessHigh))
                                        }
                                    } else if (swipeOffsetX.value < -150f) {
                                        onSkipNext()
                                        launch {
                                            swipeOffsetX.animateTo(-800f, tween(150, easing = LinearOutSlowInEasing))
                                            swipeOffsetX.snapTo(800f)
                                            swipeOffsetX.animateTo(0f, spring(stiffness = Spring.StiffnessHigh))
                                        }
                                    } else {
                                        swipeOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                                    }
                                }
                            },
                            onDragCancel = { scope.launch { swipeOffsetX.animateTo(0f) } }
                        )
                    }
                    .graphicsLayer { translationX = swipeOffsetX.value }
                    .padding(horizontal = spacing.m, vertical = spacing.s),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(ReproductorThemeTokens.shapes.small)
                        .background(colors.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "♪", color = colors.accent)
                }
                
                Spacer(modifier = Modifier.width(spacing.m))
                
                AnimatedContent(
                    targetState = state.currentSong,
                    transitionSpec = {
                        fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                    },
                    modifier = Modifier.weight(1f),
                    label = "SongMetadataTransition"
                ) { song ->
                    Column {
                        Text(
                            text = song?.title ?: "",
                            style = AppType.body.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song?.artist?.uppercase() ?: "UNKNOWN",
                            style = AppType.label.copy(fontSize = 10.sp),
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                IconButton(onClick = onPlayPauseClick) {
                    Text(
                        text = if (state.isPlaying) "‖" else "▶",
                        style = AppType.title,
                        color = colors.textPrimary
                    )
                }
            }
            
            if (state.durationMs != null && state.durationMs > 0) {
                Slider(
                    value = if (isDragging) sliderValue else state.positionMs.toFloat(),
                    onValueChange = {
                        isDragging = true
                        sliderValue = it
                    },
                    onValueChangeFinished = {
                        isDragging = false
                        onSeek(sliderValue.toLong())
                    },
                    valueRange = 0f..state.durationMs.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .padding(horizontal = spacing.m),
                    colors = SliderDefaults.colors(
                        thumbColor = colors.textPrimary,
                        activeTrackColor = colors.accent,
                        inactiveTrackColor = colors.outline
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = spacing.m, end = spacing.m, bottom = spacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(if (isDragging) sliderValue.toLong() else state.positionMs),
                        style = AppType.timer,
                        color = colors.textSecondary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = formatDuration(state.durationMs),
                        style = AppType.timer,
                        color = colors.textSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState(colors: AppColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = colors.accent, strokeWidth = 2.dp)
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
