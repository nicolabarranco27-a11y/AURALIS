// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.domain.model

/**
 * Resumen de una playlist para listados: incluye estadisticas calculadas
 * (numero de canciones visibles y duracion total) y una portada de referencia.
 */
data class PlaylistSummary(
    val id: PlaylistId,
    val name: String,
    val description: String? = null,
    val songCount: Int = 0,
    val totalDurationMs: Long = 0L,
    val coverReference: String? = null,
)