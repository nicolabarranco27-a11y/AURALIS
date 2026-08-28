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
) {
    companion object {
        val IDLE = PlaybackState()
    }
}
