package com.auralis.player.data.repository

import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.QueueItem
import com.auralis.player.domain.model.RepeatMode
import com.auralis.player.domain.repository.PlaybackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Estado de reproduccion en memoria, sin motor real.
 * En la fase de audio sera reemplazado/adaptado por la implementacion
 * que delega en MediaController; el contrato del dominio no cambia.
 */
@Singleton
class InMemoryPlaybackRepository @Inject constructor() : PlaybackRepository {

    private val _state = MutableStateFlow(PlaybackState.IDLE)

    override fun observePlaybackState(): Flow<PlaybackState> = _state.asStateFlow()

    private var queue: List<QueueItem> = emptyList()
    private var currentIndex: Int = NO_INDEX
    private var lastQueuedIndex: Int = NO_INDEX

    override suspend fun getCurrentState(): PlaybackState = _state.value

    override fun play() {
        if (queue.isEmpty()) return
        update { it.copy(isPlaying = true) }
    }

    override fun pause() {
        update { it.copy(isPlaying = false) }
    }

    override fun seekTo(positionMs: Long) {
        val duration = _state.value.durationMs ?: return
        update {
            it.copy(positionMs = positionMs.coerceIn(0L, duration))
        }
    }

    override fun skipNext() {
        moveTo(currentIndex + 1)
    }

    override fun skipPrevious() {
        if (_state.value.positionMs > RESTART_THRESHOLD_MS) {
            seekTo(0L)
        } else {
            moveTo(currentIndex - 1)
        }
    }

    override fun setShuffleEnabled(enabled: Boolean) {
        update { it.copy(shuffleEnabled = enabled) }
    }

    override fun setRepeatMode(mode: RepeatMode) {
        update { it.copy(repeatMode = mode) }
    }

    override fun setQueue(items: List<QueueItem>, startIndex: Int) {
        queue = items
        currentIndex = if (items.isEmpty()) {
            NO_INDEX
        } else {
            startIndex.coerceIn(0, items.lastIndex)
        }
        lastQueuedIndex = currentIndex
        update { it.copyFromQueue() }
    }

    override fun addSongToQueue(song: com.auralis.player.domain.model.Song) {
        val targetIndex = if (lastQueuedIndex < currentIndex) {
            currentIndex + 1
        } else {
            lastQueuedIndex + 1
        }

        val mutableQueue = queue.toMutableList()
        val newItem = QueueItem(java.util.UUID.randomUUID().toString(), song)
        
        if (targetIndex > mutableQueue.size) {
            mutableQueue.add(newItem)
        } else {
            mutableQueue.add(targetIndex, newItem)
        }
        
        queue = mutableQueue
        
        if (currentIndex == NO_INDEX) {
            currentIndex = 0
            update { it.copyFromQueue() }
        }
        
        lastQueuedIndex = targetIndex
    }

    private fun moveTo(index: Int) {
        if (queue.isEmpty()) return
        val bounded = when {
            index < 0 -> if (_state.value.repeatMode == RepeatMode.ALL) queue.lastIndex else return
            index > queue.lastIndex -> if (_state.value.repeatMode == RepeatMode.ALL) 0 else return
            else -> index
        }
        currentIndex = bounded
        update { it.copyFromQueue().copy(isPlaying = true) }
    }

    private fun PlaybackState.copyFromQueue(): PlaybackState {
        if (queue.isEmpty() || currentIndex !in queue.indices) {
            return PlaybackState.IDLE
        }
        val song = queue[currentIndex].song
        return copy(
            currentSong = song,
            durationMs = song.durationMs,
            positionMs = 0L,
        )
    }

    private fun update(transform: (PlaybackState) -> PlaybackState) {
        _state.value = transform(_state.value)
    }

    private companion object {
        const val NO_INDEX = -1
        const val RESTART_THRESHOLD_MS = 3000L
    }
}
