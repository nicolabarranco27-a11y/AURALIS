package com.auralis.player.feature.home

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralis.player.data.scanner.LibraryScanMonitor
import com.auralis.player.data.scanner.LibraryScanScheduler
import com.auralis.player.data.scanner.ScanOutcome
import com.auralis.player.data.scanner.hasAudioAccess
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.repository.LibraryRepository
import com.auralis.player.domain.repository.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ScanDisplayStatus {
    data object Idle : ScanDisplayStatus
    data object Running : ScanDisplayStatus
    data class Success(val added: Int, val updated: Int, val removed: Int, val scanned: Int) :
        ScanDisplayStatus
    data class Failed(val message: String) : ScanDisplayStatus
}

data class HomeUiState(
    val hasPermission: Boolean = false,
    val scanStatus: ScanDisplayStatus = ScanDisplayStatus.Idle,
    val songs: List<Song> = emptyList(),
    val playbackState: com.auralis.player.domain.model.PlaybackState = com.auralis.player.domain.model.PlaybackState.IDLE,
) {

    val songCount: Int get() = songs.size
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val libraryRepository: LibraryRepository,
    private val playbackRepository: PlaybackRepository,
    private val scanScheduler: LibraryScanScheduler,
    private val scanMonitor: LibraryScanMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(hasPermission = context.hasAudioAccess()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                libraryRepository.observeSongs(),
                scanMonitor.status,
                playbackRepository.observePlaybackState(),
            ) { songs, status, playback ->
                HomeUiState(
                    hasPermission = context.hasAudioAccess(),
                    scanStatus = status.toDisplayStatus(),
                    songs = songs,
                    playbackState = playback,
                )
            }.collect { newState -> _uiState.update { _ -> newState } }
        }
    }

    fun onSongClick(song: Song) {
        val currentSongs = uiState.value.songs
        val index = currentSongs.indexOf(song).coerceAtLeast(0)
        val queueItems = currentSongs.map { 
            com.auralis.player.domain.model.QueueItem(java.util.UUID.randomUUID().toString(), it)
        }
        playbackRepository.setQueue(queueItems, index)
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
        // Called when screen becomes visible
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
}