// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.QueueItem
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.repository.FavoritesRepository
import com.auralis.player.domain.repository.LibraryRepository
import com.auralis.player.domain.repository.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val songs: List<Song> = emptyList(),
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val isLoading: Boolean = true,
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val playbackRepository: PlaybackRepository,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                libraryRepository.observeSongs(),
                favoritesRepository.observeFavorites(),
                playbackRepository.observePlaybackState(),
            ) { songs, favorites, playback ->
                FavoritesUiState(
                    songs = songs.filter { it.id in favorites },
                    playbackState = playback,
                    isLoading = false,
                )
            }.collect { newState -> _uiState.update { newState } }
        }
    }

    fun onSongClick(song: Song) {
        val favoriteSongs = uiState.value.songs
        val index = favoriteSongs.indexOf(song).coerceAtLeast(0)
        val queueItems = favoriteSongs.map { QueueItem(UUID.randomUUID().toString(), it) }
        playbackRepository.setQueue(queueItems, index)
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

    fun addToQueue(song: Song) {
        playbackRepository.addSongToQueue(song)
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
