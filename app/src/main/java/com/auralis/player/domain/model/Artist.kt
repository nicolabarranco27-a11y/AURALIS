// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.domain.model

data class Artist(
    val id: ArtistId,
    val name: String,
    val songCount: Int = 0,
    val albumCount: Int = 0,
)
