package com.auralis.player.feature.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.QueueItem
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.repository.LibraryRepository
import com.auralis.player.domain.repository.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SongsUiState(
    val songs: List<Song> = emptyList(),
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val isLoading: Boolean = true,
)

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val playbackRepository: PlaybackRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongsUiState())
    val uiState: StateFlow<SongsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                libraryRepository.observeSongs(),
                playbackRepository.observePlaybackState(),
            ) { songs, playback ->
                SongsUiState(
                    songs = songs,
                    playbackState = playback,
                    isLoading = false
                )
            }.collect { newState -> _uiState.update { newState } }
        }
    }

    fun onSongClick(song: Song) {
        val currentSongs = uiState.value.songs
        val index = currentSongs.indexOf(song).coerceAtLeast(0)
        val queueItems = currentSongs.map {
            QueueItem(UUID.randomUUID().toString(), it)
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
}
