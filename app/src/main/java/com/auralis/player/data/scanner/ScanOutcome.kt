// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.data.scanner

/** Resultado de una sincronizacion completa del catalogo. */
sealed interface ScanOutcome {

    data class Success(
        val scanned: Int,
        val added: Int,
        val updated: Int,
        val removed: Int,
    ) : ScanOutcome

    data object PermissionDenied : ScanOutcome

    data class Failed(val message: String) : ScanOutcome
}
