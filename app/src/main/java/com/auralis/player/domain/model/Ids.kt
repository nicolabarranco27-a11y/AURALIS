package com.auralis.player.domain.model

/**
 * Identificadores fuertes del dominio.
 * Basados en String para facilitar serializacion y portabilidad futura (PC).
 */
@JvmInline
value class SongId(val value: String)

@JvmInline
value class AlbumId(val value: String)

@JvmInline
value class ArtistId(val value: String)

@JvmInline
value class GenreId(val value: String)

@JvmInline
value class PlaylistId(val value: String)
