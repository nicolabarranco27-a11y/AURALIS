// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

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
