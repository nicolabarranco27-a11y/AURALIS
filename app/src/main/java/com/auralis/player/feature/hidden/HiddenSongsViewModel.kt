// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.feature.hidden

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

data class HiddenSongsUiState(
    val songs: List<Song> = emptyList(),
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val isLoading: Boolean = true,
)

@HiltViewModel
class HiddenSongsViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val playbackRepository: PlaybackRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HiddenSongsUiState())
    val uiState: StateFlow<HiddenSongsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                libraryRepository.observeHiddenSongs(),
                playbackRepository.observePlaybackState(),
            ) { songs, playback ->
                HiddenSongsUiState(
                    songs = songs,
                    playbackState = playback,
                    isLoading = false,
                )
            }.collect { newState -> _uiState.update { newState } }
        }
    }

    fun restoreSong(song: Song) {
        viewModelScope.launch {
            libraryRepository.setSongHidden(song.id, false)
        }
    }

    fun restoreAll() {
        viewModelScope.launch {
            uiState.value.songs.forEach { libraryRepository.setSongHidden(it.id, false) }
        }
    }

    fun onSongClick(song: Song) {
        val currentSongs = uiState.value.songs
        val index = currentSongs.indexOf(song).coerceAtLeast(0)
        val queueItems = currentSongs.map { QueueItem(UUID.randomUUID().toString(), it) }
        playbackRepository.setQueue(queueItems, index)
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