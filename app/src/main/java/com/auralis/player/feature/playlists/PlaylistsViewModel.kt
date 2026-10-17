// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.feature.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralis.player.domain.model.PlaylistId
import com.auralis.player.domain.model.PlaylistSummary
import com.auralis.player.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistsUiState(
    val playlists: List<PlaylistSummary> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistsUiState())
    val uiState: StateFlow<PlaylistsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            playlistRepository.observePlaylistSummaries().collect { list ->
                _uiState.update { it.copy(playlists = list, isLoading = false) }
            }
        }
    }

    fun createPlaylist(name: String, description: String? = null) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name, description)
        }
    }

    fun editPlaylist(id: PlaylistId, name: String, description: String?) {
        viewModelScope.launch {
            playlistRepository.renamePlaylist(id, name)
            playlistRepository.updateDescription(id, description)
        }
    }

    fun deletePlaylist(id: PlaylistId) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(id)
        }
    }
}