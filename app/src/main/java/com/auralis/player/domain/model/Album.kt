// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.domain.model

data class Album(
    val id: AlbumId,
    val title: String,
    val artist: String? = null,
    val year: Int? = null,
    val coverReference: String? = null,
    val songCount: Int = 0,
)
