// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "artists", indices = [Index("name")])
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val songCount: Int,
    val albumCount: Int,
)
