// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

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
