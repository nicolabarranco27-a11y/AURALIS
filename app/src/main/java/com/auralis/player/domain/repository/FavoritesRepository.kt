// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.domain.repository

import com.auralis.player.domain.model.SongId
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {

    suspend fun isFavorite(songId: SongId): Boolean

    fun observeFavorites(): Flow<Set<SongId>>

    suspend fun addFavorite(songId: SongId)

    suspend fun removeFavorite(songId: SongId)

    /** Alterna el estado y devuelve el nuevo valor. */
    suspend fun toggleFavorite(songId: SongId): Boolean
}
