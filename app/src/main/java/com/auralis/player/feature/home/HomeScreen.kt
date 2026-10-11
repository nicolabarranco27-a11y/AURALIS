package com.auralis.player.feature.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.player.R
import com.auralis.player.core.common.format.formatDuration
import com.auralis.player.core.ui.components.AuralisArtwork
import com.auralis.player.core.ui.components.QueueFeedbackOverlay
import com.auralis.player.core.ui.components.LoadingState
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.Song

@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    onPlayerClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing
    
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showQueueFeedback by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        viewModel.onScreenShown()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(state.isSearchActive) {
                if (state.isSearchActive) {
                    detectTapGestures(onTap = {
                        viewModel.onToggleSearch(false)
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                }
            },
        containerColor = colors.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box {
                HomeHeader(
                    state = state,
                    onMenuClick = onMenuClick,
                    onSearchClick = { viewModel.onToggleSearch(true) },
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    colors = colors,
                    spacing = spacing
                )
                
                if (state.isSearchActive && state.searchQuery.isNotBlank()) {
                    SearchSuggestionsOverlay(
                        songs = state.searchResults,
                        onSongClick = { 
                            viewModel.onSongClick(it)
                            viewModel.onToggleSearch(false)
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        },
                        onAddToQueue = {
                            viewModel.addToQueue(it)
                            showQueueFeedback = true
                            // Cierre inmediato al añadir a la cola
                            viewModel.onToggleSearch(false)
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        },
                        colors = colors,
                        spacing = spacing
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when {
                    !state.hasPermission -> {
                        FullScreenState(
                            title = "Acceso requerido",
                            description = "Auralis necesita permiso para leer tus archivos de audio y organizar tu música.",
                            buttonText = "Conceder permiso",
                            onButtonClick = {
                                val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    Manifest.permission.READ_MEDIA_AUDIO
                                } else {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                }
                                permissionLauncher.launch(permission)
                            },
                            colors = colors,
                            spacing = spacing
                        )
                    }
                    state.songs.isEmpty() && state.scanStatus is ScanDisplayStatus.Idle -> {
                        FullScreenState(
                            title = "Sin música",
                            description = "No hemos encontrado archivos de audio en tu dispositivo.",
                            buttonText = "Escanear ahora",
                            onButtonClick = { viewModel.requestScan() },
                            colors = colors,
                            spacing = spacing
                        )
                    }
                    state.songs.isEmpty() && state.scanStatus is ScanDisplayStatus.Running -> {
                        LoadingState(colors = colors, message = "Sincronizando...")
                    }
                    state.scanStatus is ScanDisplayStatus.Failed -> {
                        FullScreenState(
                            title = "Error de escaneo",
                            description = (state.scanStatus as ScanDisplayStatus.Failed).message,
                            buttonText = "Reintentar",
                            onButtonClick = { viewModel.requestScan() },
                            iconText = "!",
                            colors = colors,
                            spacing = spacing
                        )
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            MainContent(
                                state = state,
                                onScanClick = { viewModel.requestScan() },
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
                                onToggleShuffle = { viewModel.toggleShuffle() },
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
}

@Composable
private fun HomeHeader(
    state: HomeUiState,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    val focusRequester = remember { FocusRequester() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = spacing.l, vertical = spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo Auralis
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 20.dp),
                    onClick = onMenuClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_auralis),
                contentDescription = "Logo Auralis",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )
        }
        
        Spacer(modifier = Modifier.width(spacing.m))

        Box(modifier = Modifier.weight(1f)) {
            if (state.isSearchActive) {
                TextField(
                    value = state.searchQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 0.dp)
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = "Buscar...",
                            style = AppType.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Light),
                            color = colors.textSecondary.copy(alpha = 0.5f)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        cursorColor = colors.accent
                    ),
                    textStyle = AppType.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Light),
                    singleLine = true
                )
                
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            } else {
                Column(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = onMenuClick
                        )
                ) {
                    Text(
                        text = "Auralis",
                        style = AppType.title.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Tu música, organizada.",
                        style = AppType.label.copy(fontSize = 10.sp),
                        color = colors.textSecondary.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
        
        IconButton(
            onClick = onSearchClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Buscar",
                tint = if (state.isSearchActive) colors.accent else colors.textSecondary
            )
        }
    }
}

@Composable
private fun SearchSuggestionsOverlay(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    val density = LocalDensity.current
    val offsetY = remember(density) { with(density) { 80.dp.roundToPx() } }

    Popup(
        alignment = Alignment.TopCenter,
        offset = androidx.compose.ui.unit.IntOffset(0, offsetY),
        properties = PopupProperties(focusable = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = spacing.l)
                .fillMaxWidth(0.7f),
            color = colors.surfaceRaised,
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, colors.outline)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                if (songs.isEmpty()) {
                    Text(
                        text = "Sin resultados",
                        style = AppType.label,
                        color = colors.textSecondary.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(horizontal = spacing.m, vertical = 12.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                } else {
                    songs.forEach { song ->
                        SearchSuggestionItem(
                            song = song,
                            onClick = { onSongClick(song) },
                            onSwipeRight = { onAddToQueue(song) },
                            colors = colors,
                            spacing = spacing
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSuggestionItem(
    song: Song,
    onClick: () -> Unit,
    onSwipeRight: () -> Unit,
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
                        offsetX = (offsetX + dragAmount).coerceIn(0f, 200f)
                    },
                    onDragEnd = {
                        if (offsetX > 100f) {
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
            .background(if (offsetX > 0) colors.accent.copy(alpha = 0.2f * (offsetX / 100f).coerceAtMost(1f)) else Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = offsetX }
                .clickable(enabled = offsetX == 0f, onClick = onClick)
                .padding(horizontal = spacing.m, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuralisArtwork(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(4.dp)),
                artworkReference = song.coverReference
            )
            
            Spacer(modifier = Modifier.width(spacing.s))
            
            Column {
                Text(
                    text = song.title,
                    style = AppType.body.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist ?: "Desconocido",
                    style = AppType.label.copy(fontSize = 10.sp),
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MainContent(
    state: HomeUiState,
    onScanClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onPlayPauseClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    var selectedFilter by remember { mutableStateOf("Todas") }

    val filteredSongs = remember(state.songs, selectedFilter) {
        when (selectedFilter) {
            "Favoritos" -> state.songs.filter { it.isFavorite }
            else -> state.songs
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // TU BIBLIOTECA section
            item {
                val albumCount = remember(state.songs) {
                    state.songs.mapNotNull { it.album }.toSet().size.let { if (it > 0) it else 4 }
                }
                SectionHeader(
                    title = "TU BIBLIOTECA",
                    subtitle = "${state.songs.size} canciones · $albumCount álbumes",
                    colors = colors,
                    spacing = spacing
                )
            }

            // ACCESO RÁPIDO section (Carrusel Horizontal)
            item {
                QuickAccessRow(
                    songs = state.songs,
                    onSongClick = onSongClick,
                    colors = colors,
                    spacing = spacing
                )
            }

            // CANCIONES section
            item {
                Spacer(modifier = Modifier.height(spacing.s))
                SectionHeader(
                    title = "CANCIONES",
                    subtitle = null,
                    trailingContent = {
                        IconButton(onClick = onToggleShuffle) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = "Aleatorio",
                                tint = if (state.playbackState.shuffleEnabled) colors.accent else colors.textSecondary
                            )
                        }
                    },
                    colors = colors,
                    spacing = spacing
                )
            }

            // BARRA DE FILTROS (Chips)
            item {
                FilterChipsRow(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    colors = colors,
                    spacing = spacing
                )
            }

            if (state.scanStatus is ScanDisplayStatus.Running) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.l, vertical = spacing.s)
                            .height(1.dp)
                            .background(ReproductorThemeTokens.primaryGradient)
                    )
                }
            }

            items(filteredSongs, key = { it.id.value }) { song ->
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
            
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = onScanClick) {
                        Text(
                            text = "RE-ESCANEAR BIBLIOTECA",
                            style = AppType.label,
                            color = colors.accentDim
                        )
                    }
                }
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
private fun PlaybackBar(
    state: com.auralis.player.domain.model.PlaybackState,
    onPlayPauseClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing,
    modifier: Modifier = Modifier
) {
    val duration = state.durationMs ?: 0L
    val progress = if (duration > 0) (state.positionMs.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
    val swipeOffsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Surface(
        onClick = onPlayerClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.s),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceRaised,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
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
                                    // Resistencia sutil (0.8f) para un toque premium
                                    swipeOffsetX.snapTo(swipeOffsetX.value + dragAmount * 0.8f)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    if (swipeOffsetX.value > 150f) {
                                        // Cambio inmediato
                                        onSkipPrevious()
                                        // Animación de salida y entrada ultra rápida
                                        launch {
                                            swipeOffsetX.animateTo(
                                                targetValue = 800f,
                                                animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
                                            )
                                            swipeOffsetX.snapTo(-800f)
                                            swipeOffsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(stiffness = Spring.StiffnessHigh)
                                            )
                                        }
                                    } else if (swipeOffsetX.value < -150f) {
                                        // Cambio inmediato
                                        onSkipNext()
                                        // Animación de salida y entrada ultra rápida
                                        launch {
                                            swipeOffsetX.animateTo(
                                                targetValue = -800f,
                                                animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
                                            )
                                            swipeOffsetX.snapTo(800f)
                                            swipeOffsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(stiffness = Spring.StiffnessHigh)
                                            )
                                        }
                                    } else {
                                        // Regreso suave al centro (conservado)
                                        swipeOffsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    swipeOffsetX.animateTo(0f)
                                }
                            }
                        )
                    }
                    .graphicsLayer { translationX = swipeOffsetX.value }
                    .padding(horizontal = spacing.m, vertical = spacing.s),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuralisArtwork(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    artworkReference = state.currentSong?.coverReference
                )
                
                Spacer(modifier = Modifier.width(spacing.m))
                
                AnimatedContent(
                    targetState = state.currentSong,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                    },
                    modifier = Modifier.weight(1f),
                    label = "SongMetadataTransition"
                ) { song ->
                    Column {
                        Text(
                            text = song?.title ?: "",
                            style = AppType.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
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
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pausa" else "Reproducir",
                        tint = colors.textPrimary
                    )
                }
            }
            
            if (duration > 0) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = colors.accent,
                    trackColor = colors.outline.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String?,
    trailingContent: @Composable (() -> Unit)? = null,
    colors: AppColors,
    spacing: AppSpacing
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.l, vertical = spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = title,
                style = AppType.label.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                letterSpacing = 2.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = AppType.body.copy(fontSize = 13.sp),
                    color = colors.textSecondary
                )
            }
        }
        trailingContent?.invoke()
    }
}

@Composable
private fun QuickAccessRow(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    val quickSongs = remember(songs) {
        val favorites = songs.filter { it.isFavorite }
        if (favorites.isNotEmpty()) favorites.take(5) else songs.take(5)
    }

    if (quickSongs.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = spacing.l),
                horizontalArrangement = Arrangement.spacedBy(spacing.m)
            ) {
                items(quickSongs, key = { "quick_${it.id.value}" }) { song ->
                    QuickAccessCard(
                        song = song,
                        onClick = { onSongClick(song) },
                        colors = colors,
                        spacing = spacing
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    song: Song,
    onClick: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
    ) {
        AuralisArtwork(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp)),
            artworkReference = song.coverReference
        )
        Spacer(modifier = Modifier.height(spacing.xs))
        Text(
            text = song.title,
            style = AppType.body.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist ?: "Desconocido",
            style = AppType.label.copy(fontSize = 9.sp),
            color = colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    val filters = listOf("Todas", "Favoritos")

    LazyRow(
        contentPadding = PaddingValues(horizontal = spacing.l),
        horizontalArrangement = Arrangement.spacedBy(spacing.s),
        modifier = Modifier.padding(vertical = spacing.xs)
    ) {
        items(filters) { filter ->
            val isSelected = filter == selectedFilter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter,
                        style = AppType.label.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = colors.surfaceRaised,
                    labelColor = colors.textSecondary,
                    selectedContainerColor = colors.accent,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = colors.outline.copy(alpha = 0.3f),
                    selectedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(percent = 50)
            )
        }
    }
}

@Composable
private fun FullScreenState(
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing,
    iconText: String = "●"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = iconText,
            style = AppType.display.copy(fontSize = 48.sp),
            color = if (iconText == "!") colors.onError else colors.accentDim
        )
        Spacer(modifier = Modifier.height(spacing.l))
        Text(
            text = title.uppercase(),
            style = AppType.title,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(spacing.s))
        Text(
            text = description,
            style = AppType.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = spacing.m)
        )
        Spacer(modifier = Modifier.height(spacing.xl))
        Button(
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = colors.background
            ),
            shape = ReproductorThemeTokens.shapes.small,
            contentPadding = PaddingValues(horizontal = spacing.xl, vertical = spacing.m)
        ) {
            Text(
                text = buttonText.uppercase(),
                style = AppType.label.copy(letterSpacing = 2.sp)
            )
        }
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
            .background(if (offsetX > 0) colors.accent.copy(alpha = 0.2f * (offsetX / 100f).coerceAtMost(1f)) else Color.Transparent)
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
                    .size(44.dp)
                    .clip(ReproductorThemeTokens.shapes.small),
                artworkReference = song.coverReference
            )

            Spacer(modifier = Modifier.width(spacing.m))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = AppType.body.copy(fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium),
                    color = if (isCurrent) colors.accent else colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = (song.artist ?: "Artista desconocido").uppercase(),
                    style = AppType.label.copy(fontSize = 10.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Normal),
                    color = if (isCurrent) colors.accentSecondary.copy(alpha = 0.7f) else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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

