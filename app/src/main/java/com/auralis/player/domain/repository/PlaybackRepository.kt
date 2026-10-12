package com.auralis.player.domain.repository

import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.QueueItem
import com.auralis.player.domain.model.RepeatMode
import com.auralis.player.domain.model.Song
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

    /** Añade una cancion al final de la cola actual sin interrumpir. */
    fun addSongToQueue(song: Song)

    /** Emite la cola actual en orden de reproduccion. */
    fun observeQueue(): Flow<List<Song>>

    /** Mueve una cancion dentro de la cola. */
    fun moveQueueItem(fromIndex: Int, toIndex: Int)

    /** Elimina la cancion en [index] de la cola. */
    fun removeQueueItem(index: Int)

    /** Aplica y persiste los niveles de banda del ecualizador. */
    fun applyEqualizerBands(bands: List<Float>, preset: String)
}
