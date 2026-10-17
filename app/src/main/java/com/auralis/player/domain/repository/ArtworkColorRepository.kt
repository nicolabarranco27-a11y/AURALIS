// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.domain.repository

/**
 * Obtiene un color representativo (ARGB) a partir de la referencia de una
 * caratula. Devuelve null cuando no hay portada o no aporta un color util.
 */
interface ArtworkColorRepository {
    suspend fun dominantColorFor(artworkReference: String?): Int?
}
