package com.auralis.player.domain.repository

/**
 * Obtiene un color representativo (ARGB) a partir de la referencia de una
 * caratula. Devuelve null cuando no hay portada o no aporta un color util.
 */
interface ArtworkColorRepository {
    suspend fun dominantColorFor(artworkReference: String?): Int?
}
