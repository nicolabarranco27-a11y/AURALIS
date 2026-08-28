package com.auralis.player.feature.artists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralis.player.domain.model.Artist
import com.auralis.player.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistsUiState(
    val artists: List<Artist> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class ArtistsViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArtistsUiState())
    val uiState: StateFlow<ArtistsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            libraryRepository.observeArtists().collect { artists ->
                _uiState.update { it.copy(artists = artists, isLoading = false) }
            }
        }
    }
}
