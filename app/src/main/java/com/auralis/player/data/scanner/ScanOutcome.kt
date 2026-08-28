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
