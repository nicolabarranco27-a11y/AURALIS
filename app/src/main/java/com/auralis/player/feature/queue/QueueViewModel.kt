// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.feature.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.QueueItem
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.repository.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class QueueUiState(
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val playbackState: PlaybackState = PlaybackState.IDLE,
)

@HiltViewModel
class QueueViewModel @Inject constructor(
    private val playbackRepository: PlaybackRepository,
) : ViewModel() {

    val uiState: StateFlow<QueueUiState> = combine(
        playbackRepository.observeQueue(),
        playbackRepository.observePlaybackState(),
    ) { queue, playback ->
        QueueUiState(
            queue = queue,
            currentIndex = queue.indexOfFirst { it.id == playback.currentSong?.id },
            playbackState = playback,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = QueueUiState(),
    )

    fun onSongClick(index: Int) {
        val current = uiState.value
        if (index !in current.queue.indices) return
        val items = current.queue.map { QueueItem(it.id.value, it) }
        playbackRepository.setQueue(items, index)
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        playbackRepository.moveQueueItem(fromIndex, toIndex)
    }

    fun removeQueueItem(index: Int) {
        playbackRepository.removeQueueItem(index)
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
