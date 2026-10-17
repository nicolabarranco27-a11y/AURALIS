// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "albums", indices = [Index("title"), Index("artist")])
data class AlbumEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String?,
    val year: Int?,
    val coverUri: String?,
    val songCount: Int,
)
