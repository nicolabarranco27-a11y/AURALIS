package com.auralis.player.domain.model

import java.time.Instant

/**
 * Cancion del dominio, independiente de Android y de la base de datos.
 *
 * [sourceUri] es una referencia de origen abstracta (string opaco):
 * la capa de datos decide como materializarla en cada plataforma.
 * Los metadatos opcionales se modelan como null: el dominio nunca
 * inventa valores sustitutos como "Unknown Artist".
 */
data class Song(
    val id: SongId,
    val sourceUri: String,
    val title: String,
    val artist: String? = null,
    val album: String? = null,
    val albumArtist: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val durationMs: Long? = null,
    val dateAdded: Instant? = null,
    val playCount: Long = 0L,
    val lastPlayedAt: Instant? = null,
    val isFavorite: Boolean = false,
    val coverReference: String? = null,
)
