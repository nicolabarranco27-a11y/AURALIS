package com.auralis.player.domain.model

import java.time.Instant

data class Playlist(
    val id: PlaylistId,
    val name: String,
    val description: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isSystemPlaylist: Boolean = false,
)
