// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

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

/**
 * Formatea una duracion larga (p. ej. la suma de una playlist) como
 * "12 min" o "1 h 05 min". Nunca muestra segundos.
 */
internal fun formatTotalDuration(durationMs: Long): String {
    val totalMinutes = durationMs / 60_000
    return when {
        totalMinutes >= 60 -> {
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            if (minutes == 0L) "${hours} h" else "${hours} h ${minutes} min"
        }
        else -> "${totalMinutes} min"
    }
}

/** Devuelve "1 canción" o "N canciones". */
internal fun formatSongCount(count: Int): String =
    if (count == 1) "1 canción" else "$count canciones"
