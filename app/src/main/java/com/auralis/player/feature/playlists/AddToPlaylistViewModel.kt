package com.auralis.player.feature.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralis.player.domain.model.PlaylistId
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import com.auralis.player.domain.repository.LibraryRepository
import com.auralis.player.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddToPlaylistUiState(
    val allSongs: List<Song> = emptyList(),
    val playlistSongsIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class AddToPlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val libraryRepository: LibraryRepository,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val playlistIdString: String = checkNotNull(savedStateHandle["playlistId"])
    private val playlistId = PlaylistId(playlistIdString)

    private val _uiState = MutableStateFlow(AddToPlaylistUiState())
    val uiState: StateFlow<AddToPlaylistUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                libraryRepository.observeSongs(),
                playlistRepository.observeSongs(playlistId)
            ) { allSongs, pSongs ->
                AddToPlaylistUiState(
                    allSongs = allSongs,
                    playlistSongsIds = pSongs.map { it.id.value }.toSet(),
                    isLoading = false
                )
            }.collect { newState -> _uiState.update { newState } }
        }
    }

    fun toggleSongInPlaylist(songId: SongId) {
        viewModelScope.launch {
            if (uiState.value.playlistSongsIds.contains(songId.value)) {
                playlistRepository.removeSong(playlistId, songId)
            } else {
                playlistRepository.addSong(playlistId, songId)
            }
        }
    }
}
