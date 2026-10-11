package com.auralis.player.core.common.format

/**
 * Formatea una duracion en milisegundos como "m:ss".
 * Utilidad compartida por las pantallas del reproductor.
 */
internal fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
