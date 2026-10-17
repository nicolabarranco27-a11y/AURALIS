// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

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
