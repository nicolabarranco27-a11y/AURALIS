// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.data.repository

import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.QueueItem
import com.auralis.player.domain.model.RepeatMode
import com.auralis.player.domain.model.Song
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

    private val _queueFlow = MutableStateFlow<List<Song>>(emptyList())

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
        publishQueue()
        update { it.copyFromQueue() }
    }

    override fun addSongToQueue(song: Song) {
        val targetIndex = QueueInsertIndexCalculator.calculate(
            currentIndex = currentIndex,
            lastQueuedIndex = lastQueuedIndex,
            itemCount = queue.size,
        )

        val mutableQueue = queue.toMutableList()
        val newItem = QueueItem(java.util.UUID.randomUUID().toString(), song)
        mutableQueue.add(targetIndex, newItem)
        queue = mutableQueue

        if (currentIndex == NO_INDEX) {
            currentIndex = 0
            update { it.copyFromQueue() }
        }

        lastQueuedIndex = targetIndex
        publishQueue()
    }

    override fun observeQueue(): Flow<List<Song>> = _queueFlow.asStateFlow()

    override fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in queue.indices) return
        val target = toIndex.coerceIn(0, queue.lastIndex)
        if (fromIndex == target) return

        val mutableQueue = queue.toMutableList()
        val moved = mutableQueue.removeAt(fromIndex)
        mutableQueue.add(target, moved)
        queue = mutableQueue

        currentIndex = if (currentIndex == fromIndex) {
            target
        } else {
            var adjusted = currentIndex
            if (adjusted > fromIndex) adjusted--
            if (adjusted >= target) adjusted++
            adjusted.coerceIn(0, queue.lastIndex)
        }
        lastQueuedIndex = NO_INDEX
        publishQueue()
        update { it.copyFromQueue() }
    }

    override fun removeQueueItem(index: Int) {
        if (index !in queue.indices) return

        val mutableQueue = queue.toMutableList()
        mutableQueue.removeAt(index)
        queue = mutableQueue

        currentIndex = when {
            index < currentIndex -> currentIndex - 1
            index == currentIndex -> currentIndex.coerceAtMost(queue.lastIndex)
            else -> currentIndex
        }
        if (queue.isEmpty()) currentIndex = NO_INDEX
        lastQueuedIndex = NO_INDEX
        publishQueue()
        update { it.copyFromQueue() }
    }

    override fun applyEqualizerBands(bands: List<Float>, preset: String) {
        // Sin motor de audio real no hay efecto que aplicar.
    }

    private fun publishQueue() {
        _queueFlow.value = queue.map { it.song }
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
