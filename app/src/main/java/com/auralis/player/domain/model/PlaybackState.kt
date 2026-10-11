package com.auralis.player.domain.model

/**
 * Estado logico de reproduccion, sin tipos del motor concreto.
 * [currentSong] null representa ausencia de reproduccion.
 */
data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long? = null,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val audioSessionId: Int = 0,
    /** Mensaje del ultimo error de reproduccion, o null si no hay error. */
    val errorMessage: String? = null,
) {
    companion object {
        val IDLE = PlaybackState()
    }
}
