// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.domain.model

/**
 * Elemento de la cola de reproduccion.
 * La cola del dominio es independiente del motor de reproduccion
 * concreto; la traduccion a Media3 ocurre en la capa de playback.
 */
data class QueueItem(
    val uid: String,
    val song: Song,
)
