package com.auralis.player.core.common.library

import com.auralis.player.domain.model.Song

/**
 * Selecciona las canciones de "acceso rapido" para Inicio:
 * - primero las mas reproducidas (`playCount`), desempatando por la ultima
 *   reproduccion mas reciente;
 * - si aun no hay reproducciones registradas, las favoritas;
 * - y si tampoco hay favoritas, las primeras [limit].
 */
fun mostPlayedSongs(songs: List<Song>, limit: Int = 5): List<Song> {
    if (limit <= 0 || songs.isEmpty()) return emptyList()

    val played = songs
        .filter { it.playCount > 0 }
        .sortedWith(
            compareByDescending<Song> { it.playCount }
                .thenByDescending { it.lastPlayedAt?.toEpochMilli() ?: Long.MIN_VALUE },
        )
    if (played.isNotEmpty()) return played.take(limit)

    val favorites = songs.filter { it.isFavorite }
    return (if (favorites.isNotEmpty()) favorites else songs).take(limit)
}
