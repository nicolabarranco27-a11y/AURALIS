// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

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
