// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.feature.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Folder
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
import com.auralis.player.core.common.folder.songsInSelectedFolder
import com.auralis.player.core.common.format.formatSongCount
import com.auralis.player.core.common.format.formatTotalDuration
import com.auralis.player.core.ui.components.AuralisArtwork
import com.auralis.player.core.ui.components.ConfirmDeleteDialog
import com.auralis.player.core.ui.components.CreatePlaylistDialog
import com.auralis.player.core.ui.components.MiniPlayerBar
import com.auralis.player.core.ui.components.QueueFeedbackOverlay
import com.auralis.player.core.ui.components.SongActionsSheet
import com.auralis.player.core.ui.components.SongListItem
import com.auralis.player.core.ui.components.songActions
import com.auralis.player.core.ui.components.LoadingState
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.domain.model.PlaylistId
import com.auralis.player.domain.model.PlaylistSummary
import com.auralis.player.domain.model.Song
import com.auralis.player.feature.folders.MusicFoldersSheet
import com.auralis.player.feature.folders.rememberFolderPickerLauncher

@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onPlaylistClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing

    var showFolderSheet by remember { mutableStateOf(false) }
    val openFolderPicker = rememberFolderPickerLauncher { uri -> viewModel.addFolder(uri) }
    
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showQueueFeedback by remember { mutableStateOf(false) }
    var actionSong by remember { mutableStateOf<Song?>(null) }

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
                    state.songs.isEmpty() && state.scanStatus is ScanDisplayStatus.Running -> {
                        LoadingState(colors = colors, message = "Sincronizando...")
                    }
                    state.songs.isEmpty() && state.scanStatus is ScanDisplayStatus.Failed -> {
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
                    // Solo se muestra la pantalla guiada cuando de verdad no hay
                    // nada que ver. Si existe una biblioteca vacia pero hay
                    // playlists o carpetas, se sigue mostrando Inicio para que
                    // esos elementos sigan siendo accesibles (nunca "desaparecen").
                    state.songs.isEmpty() && state.playlists.isEmpty() && state.folders.isEmpty() -> {
                        FullScreenState(
                            title = "Sin música",
                            description = "No encontramos música en tus carpetas. Agrega carpetas para comenzar.",
                            buttonText = "Agregar carpetas",
                            onButtonClick = { showFolderSheet = true },
                            secondaryButtonText = "Escanear ahora",
                            onSecondaryButtonClick = { viewModel.requestScan() },
                            colors = colors,
                            spacing = spacing
                        )
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            MainContent(
                                state = state,
                                onScanClick = { viewModel.requestScan() },
                                onPlayScoped = { song, scope -> viewModel.onSongClick(song, scope) },
                                onPlayAll = { scope -> viewModel.playAll(scope) },
                                onAddToQueue = { 
                                    viewModel.addToQueue(it)
                                    showQueueFeedback = true
                                },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onLongPress = { actionSong = it },
                                onPlaylistClick = onPlaylistClick,
                                onCreatePlaylist = { name, description -> viewModel.createPlaylist(name, description) },
                                onDeletePlaylist = { viewModel.deletePlaylist(it) },
                                onAddFolderClick = { showFolderSheet = true },
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

    if (showFolderSheet) {
        MusicFoldersSheet(
            folders = state.selectedFolders,
            onAddFolder = { openFolderPicker() },
            onRemoveFolder = { viewModel.removeFolder(it) },
            onDismiss = { showFolderSheet = false }
        )
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
    onPlayScoped: (Song, List<Song>) -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onLongPress: (Song) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onCreatePlaylist: (String, String?) -> Unit,
    onDeletePlaylist: (PlaylistId) -> Unit,
    onAddFolderClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    var selectedFilter by rememberSaveable { mutableStateOf("Todas") }
    var selectedFolderUri by rememberSaveable { mutableStateOf<String?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var deletingPlaylist by remember { mutableStateOf<PlaylistSummary?>(null) }
    var revealedPlaylistId by remember { mutableStateOf<String?>(null) }

    // El tacho se oculta solo a los 3 segundos si el usuario no interactua.
    LaunchedEffect(revealedPlaylistId) {
        if (revealedPlaylistId != null) {
            delay(3000)
            revealedPlaylistId = null
        }
    }

    // Canciones de la carpeta seleccionada abierta (todas las suyas).
    val folderSongs = remember(state.songs, selectedFolderUri) {
        songsInSelectedFolder(state.songs, selectedFolderUri)
    }

    val favoriteSongs = remember(state.songs, state.favoriteIds) {
        state.songs.filter { it.id in state.favoriteIds }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(revealedPlaylistId) {
                // Tocar cualquier parte de la pantalla cancela la eliminacion.
                if (revealedPlaylistId != null) {
                    detectTapGestures(onTap = { revealedPlaylistId = null })
                }
            }
    ) {
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

            // ACCESO RÁPIDO section (Carrusel Horizontal · más reproducidas)
            item {
                QuickAccessRow(
                    songs = state.popularSongs,
                    onSongPlay = onPlayScoped,
                    colors = colors,
                    spacing = spacing
                )
            }

            // CANCIONES section
            item {
                Spacer(modifier = Modifier.height(spacing.s))
                SectionHeader(
                    title = when (selectedFilter) {
                        "Favoritos" -> "FAVORITOS"
                        "Playlists" -> "PLAYLISTS"
                        else -> "CANCIONES"
                    },
                    subtitle = null,
                    trailingContent = {
                        if (selectedFilter == "Playlists") {
                            IconButton(onClick = { showCreatePlaylistDialog = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Crear playlist",
                                    tint = colors.accent
                                )
                            }
                        } else {
                            IconButton(onClick = onToggleShuffle) {
                                Icon(
                                    imageVector = Icons.Rounded.Shuffle,
                                    contentDescription = "Aleatorio",
                                    tint = if (state.playbackState.shuffleEnabled) colors.accent else colors.textSecondary
                                )
                            }
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

            // NAVEGACIÓN POR CARPETAS (solo en "Todas")
            if (selectedFilter == "Todas") {
                item(key = "folder_browser") {
                    FolderBrowser(
                        folders = state.folders,
                        selectedFolderUri = selectedFolderUri,
                        folderSongs = folderSongs,
                        onFolderOpen = { uri ->
                            selectedFolderUri = if (selectedFolderUri == uri) null else uri
                        },
                        onPlayFolder = { onPlayAll(folderSongs) },
                        onAddFolder = onAddFolderClick,
                        colors = colors,
                        spacing = spacing,
                    )
                }

                if (selectedFolderUri == null && state.folders.isNotEmpty()) {
                    item(key = "folder_hint") {
                        Text(
                            text = "Elegí una carpeta para ver su música",
                            style = AppType.body,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.l, vertical = spacing.l),
                        )
                    }
                }

                if (selectedFolderUri != null && folderSongs.isEmpty()) {
                    item(key = "folder_empty") {
                        Text(
                            text = "No hay canciones en esta carpeta",
                            style = AppType.body,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.l, vertical = spacing.l),
                        )
                    }
                }
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

            if (selectedFilter == "Playlists") {
                if (state.playlists.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.l, vertical = spacing.xl),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Aún no tienes playlists",
                                style = AppType.body,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(spacing.s))
                            TextButton(onClick = { showCreatePlaylistDialog = true }) {
                                Text("CREAR PLAYLIST", style = AppType.label, color = colors.accent)
                            }
                        }
                    }
                } else {
                    items(state.playlists, key = { it.id.value }) { playlist ->
                        PlaylistSummaryItem(
                            playlist = playlist,
                            revealed = revealedPlaylistId == playlist.id.value,
                            onClick = { onPlaylistClick(playlist.id.value) },
                            onReveal = { revealedPlaylistId = playlist.id.value },
                            onCancel = { revealedPlaylistId = null },
                            onDelete = { deletingPlaylist = playlist },
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
            } else {
                val songsForList = when (selectedFilter) {
                    "Favoritos" -> favoriteSongs
                    "Playlists" -> emptyList()
                    else -> if (selectedFolderUri != null) folderSongs else emptyList()
                }
                items(songsForList, key = { it.id.value }) { song ->
                    val isCurrent = state.playbackState.currentSong?.id == song.id
                    SongListItem(
                        song = song,
                        isCurrent = isCurrent,
                        onClick = { onPlayScoped(song, songsForList) },
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

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name, description ->
                onCreatePlaylist(name, description)
                showCreatePlaylistDialog = false
            },
            colors = colors,
            spacing = spacing,
        )
    }

    deletingPlaylist?.let { playlist ->
        ConfirmDeleteDialog(
            title = "Eliminar playlist",
            message = "¿Seguro que quieres eliminar \"${playlist.name}\"? Esta acción no se puede deshacer.",
            onDismiss = { deletingPlaylist = null },
            onConfirm = {
                onDeletePlaylist(playlist.id)
                deletingPlaylist = null
            },
            colors = colors
        )
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
    onSongPlay: (Song, List<Song>) -> Unit,
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
                        onClick = { onSongPlay(song, quickSongs) },
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
private fun PlaylistSummaryItem(
    playlist: PlaylistSummary,
    revealed: Boolean,
    onClick: () -> Unit,
    onReveal: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (revealed) onCancel() else onClick() },
                onLongClick = { if (revealed) onCancel() else onReveal() }
            )
            .padding(horizontal = spacing.l, vertical = spacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AuralisArtwork(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp)),
            artworkReference = playlist.coverReference
        )
        Spacer(modifier = Modifier.width(spacing.m))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = AppType.body.copy(fontWeight = FontWeight.Medium),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subtitle = buildString {
                append(formatSongCount(playlist.songCount))
                if (playlist.totalDurationMs > 0) {
                    append(" · ")
                    append(formatTotalDuration(playlist.totalDurationMs))
                }
            }
            Text(
                text = subtitle,
                style = AppType.label.copy(fontSize = 12.sp),
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        AnimatedVisibility(
            visible = revealed,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Eliminar playlist",
                    tint = colors.onError
                )
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    val filters = listOf("Todas", "Favoritos", "Playlists")

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
private fun FolderBrowser(
    folders: List<HomeFolder>,
    selectedFolderUri: String?,
    folderSongs: List<Song>,
    onFolderOpen: (String) -> Unit,
    onPlayFolder: () -> Unit,
    onAddFolder: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.l, vertical = spacing.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "CARPETAS (${folders.size})",
                style = AppType.label.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                color = colors.textPrimary,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectedFolderUri != null && folderSongs.isNotEmpty()) {
                    IconButton(onClick = onPlayFolder) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Reproducir carpeta",
                            tint = colors.accent,
                        )
                    }
                }
                IconButton(onClick = onAddFolder) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Añadir carpeta",
                        tint = colors.accent,
                    )
                }
            }
        }

        if (folders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.l, vertical = spacing.l),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "No hay carpetas seleccionadas",
                    style = AppType.body,
                    color = colors.textSecondary,
                )
                Spacer(modifier = Modifier.height(spacing.s))
                TextButton(onClick = onAddFolder) {
                    Text(
                        text = "AÑADIR CARPETAS",
                        style = AppType.label,
                        color = colors.accent,
                    )
                }
            }
            return@Column
        }

        val listState = rememberLazyListState()
        LaunchedEffect(selectedFolderUri, folders) {
            val index = folders.indexOfFirst { it.uri == selectedFolderUri }
            if (index >= 0) listState.animateScrollToItem(index)
        }

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = spacing.l, vertical = spacing.s),
            horizontalArrangement = Arrangement.spacedBy(spacing.s),
        ) {
            items(folders, key = { it.uri }) { folder ->
                FolderItem(
                    folder = folder,
                    selected = folder.uri == selectedFolderUri,
                    onClick = { onFolderOpen(folder.uri) },
                    colors = colors,
                    spacing = spacing,
                )
            }
        }
    }
}

@Composable
private fun FolderItem(
    folder: HomeFolder,
    selected: Boolean,
    onClick: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing,
) {
    val shape = RoundedCornerShape(12.dp)
    val container = if (selected) colors.accent.copy(alpha = 0.18f) else colors.surfaceRaised
    val border = if (selected) colors.accent.copy(alpha = 0.6f) else Color.Transparent
    Row(
        modifier = Modifier
            .clip(shape)
            .background(container)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.m, vertical = spacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.s),
    ) {
        AuralisArtwork(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            artworkReference = folder.coverReference,
        )
        Column {
            Text(
                text = folder.name,
                style = AppType.label.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                color = if (selected) colors.accent else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatSongCount(folder.songCount),
                style = AppType.label.copy(fontSize = 10.sp),
                color = colors.textSecondary,
                maxLines = 1,
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
    iconText: String = "●",
    secondaryButtonText: String? = null,
    onSecondaryButtonClick: (() -> Unit)? = null,
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
        if (secondaryButtonText != null && onSecondaryButtonClick != null) {
            Spacer(modifier = Modifier.height(spacing.s))
            TextButton(onClick = onSecondaryButtonClick) {
                Text(
                    text = secondaryButtonText,
                    style = AppType.label,
                    color = colors.textSecondary
                )
            }
        }
    }
}

