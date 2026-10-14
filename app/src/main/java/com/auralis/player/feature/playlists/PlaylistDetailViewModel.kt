package com.auralis.player.feature.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.Playlist
import com.auralis.player.domain.model.PlaylistId
import com.auralis.player.domain.model.QueueItem
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import com.auralis.player.domain.repository.FavoritesRepository
import com.auralis.player.domain.repository.LibraryRepository
import com.auralis.player.domain.repository.PlaybackRepository
import com.auralis.player.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PlaylistDetailUiState(
    val playlist: Playlist? = null,
    val songs: List<Song> = emptyList(),
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val isLoading: Boolean = true,
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val playbackRepository: PlaybackRepository,
    private val favoritesRepository: FavoritesRepository,
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val playlistIdString: String = savedStateHandle.get<String>("playlistId").orEmpty()
    private val playlistId = PlaylistId(playlistIdString)

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val playlist = playlistRepository.getPlaylist(playlistId)
            _uiState.update { it.copy(playlist = playlist) }

            combine(
                playlistRepository.observeSongs(playlistId),
                playbackRepository.observePlaybackState(),
            ) { songs, playback ->
                PlaylistDetailUiState(
                    playlist = _uiState.value.playlist,
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

    fun playAll(shuffle: Boolean = false) {
        val currentSongs = uiState.value.songs
        if (currentSongs.isEmpty()) return
        val queueItems = currentSongs.map {
            QueueItem(UUID.randomUUID().toString(), it)
        }
        playbackRepository.setQueue(queueItems, 0)
        playbackRepository.setShuffleEnabled(shuffle)
    }

    fun addAllToQueue() {
        uiState.value.songs.forEach { playbackRepository.addSongToQueue(it) }
    }

    fun moveSong(songId: SongId, newPosition: Int) {
        viewModelScope.launch {
            playlistRepository.moveSong(playlistId, songId, newPosition)
        }
    }

    fun togglePlayPause() {
        if (uiState.value.playbackState.isPlaying) {
            playbackRepository.pause()
        } else {
            playbackRepository.play()
        }
    }

    fun removeSong(songId: SongId) {
        viewModelScope.launch {
            playlistRepository.removeSong(playlistId, songId)
        }
    }

    fun renamePlaylist(newName: String) {
        viewModelScope.launch {
            playlistRepository.renamePlaylist(playlistId, newName)
            val updated = playlistRepository.getPlaylist(playlistId)
            _uiState.update { it.copy(playlist = updated) }
        }
    }

    fun updateDetails(name: String, description: String?) {
        viewModelScope.launch {
            playlistRepository.renamePlaylist(playlistId, name)
            playlistRepository.updateDescription(playlistId, description)
            val updated = playlistRepository.getPlaylist(playlistId)
            _uiState.update { it.copy(playlist = updated) }
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
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
}
