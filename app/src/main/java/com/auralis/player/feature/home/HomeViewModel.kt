// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.feature.home

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralis.player.core.common.format.folderDisplayName
import com.auralis.player.core.common.folder.libraryFolders
import com.auralis.player.core.common.library.mostPlayedSongs
import com.auralis.player.data.scanner.LibraryScanMonitor
import com.auralis.player.data.scanner.LibraryScanScheduler
import com.auralis.player.data.scanner.ScanOutcome
import com.auralis.player.data.scanner.hasAudioAccess
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.PlaylistSummary
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import com.auralis.player.domain.repository.FavoritesRepository
import com.auralis.player.domain.repository.LibraryFolderRepository
import com.auralis.player.domain.repository.LibraryRepository
import com.auralis.player.domain.repository.PlaybackRepository
import com.auralis.player.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface ScanDisplayStatus {
    data object Idle : ScanDisplayStatus
    data object Running : ScanDisplayStatus
    data class Success(val added: Int, val updated: Int, val removed: Int, val scanned: Int) :
        ScanDisplayStatus
    data class Failed(val message: String) : ScanDisplayStatus
}

/** Carpeta elegida por el usuario, lista para mostrarse como entrada en Inicio. */
data class HomeFolder(
    val uri: String,
    val name: String,
    val songCount: Int,
    val coverReference: String?,
)

data class HomeUiState(
    val hasPermission: Boolean = false,
    val scanStatus: ScanDisplayStatus = ScanDisplayStatus.Idle,
    val songs: List<Song> = emptyList(),
    val selectedFolders: Set<String> = emptySet(),
    val favoriteIds: Set<SongId> = emptySet(),
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Song> = emptyList(),
    val playlists: List<PlaylistSummary> = emptyList(),
    val folders: List<HomeFolder> = emptyList(),
    val popularSongs: List<Song> = emptyList(),
) {

    val songCount: Int get() = songs.size
}

private data class CatalogData(
    val songs: List<Song>,
    val selectedFolders: Set<String>,
    val status: LibraryScanMonitor.Status,
    val playlists: List<PlaylistSummary>,
)

/** Derivados pesados de Inicio: solo cambian cuando cambia el catalogo. */
private data class LibraryDerived(
    val folders: List<HomeFolder>,
    val popular: List<Song>,
)

private data class CoreHome(
    val catalog: CatalogData,
    val derived: LibraryDerived,
    val playback: PlaybackState,
)


@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val libraryRepository: LibraryRepository,
    private val playbackRepository: PlaybackRepository,
    private val scanScheduler: LibraryScanScheduler,
    private val scanMonitor: LibraryScanMonitor,
    private val favoritesRepository: FavoritesRepository,
    private val folderRepository: LibraryFolderRepository,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(hasPermission = context.hasAudioAccess()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _searchResults = _searchQuery
        .transformLatest { query ->
            if (query.isNotBlank()) {
                delay(300)
                val normalizedQuery = query.trim().lowercase()
                    .replace('á', 'a')
                    .replace('é', 'e')
                    .replace('í', 'i')
                    .replace('ó', 'o')
                    .replace('ú', 'u')
                    .replace('ü', 'u')
                    .replace('ñ', 'n')
                emit(libraryRepository.searchSongs(normalizedQuery).take(4))
            } else {
                emit(emptyList())
            }
        }

    init {
        viewModelScope.launch {
            // Catalogo de canciones compartido: Room ya emite en su executor;
            // el mapping/distinct se estabiliza fuera del hilo principal y se
            // comparte entre los flujos que lo consumen.
            val songsFlow = libraryRepository.observeSongs()
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .shareIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT_MS), replay = 1)

            val selectedFoldersFlow = folderRepository.observeSelectedFolders()
                .distinctUntilChanged()
                .shareIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT_MS), replay = 1)

            // Derivados pesados (O(n)/O(n log n)) dependen SOLO del catalogo y de
            // las carpetas elegidas: nunca de los ticks de reproduccion (500 ms).
            val libraryDerivedFlow = combine(songsFlow, selectedFoldersFlow) { songs, selected ->
                LibraryDerived(
                    folders = buildFolders(songs, selected),
                    popular = mostPlayedSongs(songs, QUICK_ACCESS_LIMIT),
                )
            }
                .flowOn(Dispatchers.Default)
                .distinctUntilChanged()
                .shareIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT_MS), replay = 1)

            val catalogFlow = combine(
                songsFlow,
                selectedFoldersFlow,
                scanMonitor.status,
                playlistRepository.observePlaylistSummaries(),
            ) { songs, selected, status, playlists ->
                CatalogData(songs, selected, status, playlists)
            }
                .flowOn(Dispatchers.Default)
                .distinctUntilChanged()
                .shareIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT_MS), replay = 1)

            val coreFlow = combine(
                catalogFlow,
                libraryDerivedFlow,
                playbackRepository.observePlaybackState(),
            ) { catalog, derived, playback -> CoreHome(catalog, derived, playback) }

            combine(
                coreFlow,
                _isSearchActive,
                _searchQuery,
                _searchResults,
                favoritesRepository.observeFavorites()
            ) { core, active, query, results, favorites ->
                HomeUiState(
                    hasPermission = context.hasAudioAccess(),
                    scanStatus = core.catalog.status.toDisplayStatus(),
                    songs = core.catalog.songs,
                    selectedFolders = core.catalog.selectedFolders,
                    favoriteIds = favorites,
                    playbackState = core.playback,
                    isSearchActive = active,
                    searchQuery = query,
                    searchResults = results,
                    playlists = core.catalog.playlists,
                    folders = core.derived.folders,
                    popularSongs = core.derived.popular,
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    /**
     * Carpetas seleccionadas con su conteo y portada, ordenadas por nombre.
     * Inicio las muestra como entradas tipo playlist bajo "Todas".
     */
    private fun buildFolders(songs: List<Song>, selectedFolders: Set<String>): List<HomeFolder> =
        libraryFolders(songs, selectedFolders)
            .map { folder ->
                HomeFolder(
                    uri = folder.uri,
                    name = folderDisplayName(folder.uri),
                    songCount = folder.songCount,
                    coverReference = folder.coverReference,
                )
            }
            .sortedBy { it.name.lowercase() }

    fun onToggleSearch(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSongClick(song: Song, scope: List<Song> = uiState.value.songs) {
        if (scope.isEmpty()) return
        viewModelScope.launch {
            val index = scope.indexOf(song).coerceAtLeast(0)
            val queueItems = withContext(Dispatchers.Default) {
                scope.map {
                    com.auralis.player.domain.model.QueueItem(java.util.UUID.randomUUID().toString(), it)
                }
            }
            playbackRepository.setQueue(queueItems, index)
        }
    }

    /**
     * Reproduce una lista (p. ej. una carpeta o un filtro) desde su primer
     * tema. La cola queda limitada a [scope] para no mezclar toda la biblioteca.
     */
    fun playAll(scope: List<Song>) {
        scope.firstOrNull()?.let { onSongClick(it, scope) }
    }

    fun togglePlayPause() {
        if (uiState.value.playbackState.isPlaying) {
            playbackRepository.pause()
        } else {
            playbackRepository.play()
        }
    }

    fun seekTo(positionMs: Long) {
        playbackRepository.seekTo(positionMs)
    }

    fun skipNext() {
        playbackRepository.skipNext()
    }

    fun skipPrevious() {
        playbackRepository.skipPrevious()
    }

    fun addToQueue(song: Song) {
        playbackRepository.addSongToQueue(song)
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(song.id)
        }
    }

    fun hideSong(song: Song) {
        viewModelScope.launch {
            libraryRepository.setSongHidden(song.id, true)
        }
    }

    fun createPlaylist(name: String, description: String? = null) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name, description)
        }
    }

    fun deletePlaylist(id: com.auralis.player.domain.model.PlaylistId) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(id)
        }
    }

    fun toggleShuffle() {
        val currentShuffle = uiState.value.playbackState.shuffleEnabled
        playbackRepository.setShuffleEnabled(!currentShuffle)
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasPermission = context.hasAudioAccess()) }
        if (granted) {
            requestScan()
        }
    }

    fun requestScan() {
        if (!context.hasAudioAccess()) return
        scanScheduler.requestScan()
    }

    fun refreshPermissionState() {
        _uiState.update { it.copy(hasPermission = context.hasAudioAccess()) }
    }

    fun requestAudioPermission() {
        if (context.checkSelfPermission(
                android.Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // The screen should call this via ActivityResultContracts
            // This is a no-op here; the screen handles the actual request
        }
    }

    fun onScreenShown() {
        // Escaneo automatico de arranque: solo si ya hay permiso y la
        // biblioteca esta vacia, para no re-escaneos innecesarios.
        if (context.hasAudioAccess() && _uiState.value.songs.isEmpty()) {
            scanScheduler.requestScan()
        }
    }

    fun addFolder(treeUri: String) {
        viewModelScope.launch {
            folderRepository.addFolder(treeUri)
            scanScheduler.requestScan()
        }
    }

    fun removeFolder(treeUri: String) {
        viewModelScope.launch {
            folderRepository.removeFolder(treeUri)
            scanScheduler.requestScan()
        }
    }

    private fun LibraryScanMonitor.Status.toDisplayStatus(): ScanDisplayStatus = when (this) {
        LibraryScanMonitor.Status.Idle -> ScanDisplayStatus.Idle
        LibraryScanMonitor.Status.Running -> ScanDisplayStatus.Running
        is LibraryScanMonitor.Status.Finished -> when (val o = outcome) {
            is ScanOutcome.Success -> ScanDisplayStatus.Success(
                scanned = o.scanned,
                added = o.added,
                updated = o.updated,
                removed = o.removed,
            )
            ScanOutcome.PermissionDenied -> ScanDisplayStatus.Idle
            is ScanOutcome.Failed -> ScanDisplayStatus.Failed(o.message)
        }
    }

    private companion object {
        /** Canciones del carrusel de "acceso rapido". */
        const val QUICK_ACCESS_LIMIT = 5

        /** Mantiene vivo el catalogo compartido mientras la UI esta suscrita. */
        const val SUBSCRIBE_TIMEOUT_MS = 5_000L
    }
}