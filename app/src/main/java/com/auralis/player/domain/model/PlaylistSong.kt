package com.auralis.player.domain.model

/**
 * Relacion ordenada entre playlist y cancion.
 * [position] define el orden dentro de la playlist; reordenar
 * consiste en reasignar posiciones, sin depender de la persistencia.
 */
data class PlaylistSong(
    val playlistId: PlaylistId,
    val songId: SongId,
    val position: Int,
)
