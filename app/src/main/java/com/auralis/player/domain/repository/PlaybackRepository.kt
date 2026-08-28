package com.auralis.player.domain.repository

import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.QueueItem
import com.auralis.player.domain.model.RepeatMode
import kotlinx.coroutines.flow.Flow

/**
 * Abstraccion de las operaciones de reproduccion.
 * El dominio solo conoce este contrato; su implementacion
 * delegara en MediaController/MediaSession en la capa de playback.
 */
interface PlaybackRepository {

    fun observePlaybackState(): Flow<PlaybackState>

    suspend fun getCurrentState(): PlaybackState

    fun play()

    fun pause()

    fun seekTo(positionMs: Long)

    fun skipNext()

    fun skipPrevious()

    fun setShuffleEnabled(enabled: Boolean)

    fun setRepeatMode(mode: RepeatMode)

    /** Reemplaza la cola completa y posiciona el indice inicial. */
    fun setQueue(items: List<QueueItem>, startIndex: Int = 0)
}
