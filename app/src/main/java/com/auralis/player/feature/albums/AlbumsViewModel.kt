package com.auralis.player.feature.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralis.player.domain.model.Album
import com.auralis.player.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumsUiState(
    val albums: List<Album> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlbumsUiState())
    val uiState: StateFlow<AlbumsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            libraryRepository.observeAlbums().collect { albums ->
                _uiState.update { it.copy(albums = albums, isLoading = false) }
            }
        }
    }
}
