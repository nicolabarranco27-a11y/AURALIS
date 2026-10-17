// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Carpetas de musica que el usuario eligio indexar.
 *
 * Cada carpeta se identifica por el URI de arbol ("tree URI") que devuelve el
 * selector de documentos del sistema. El escaneo se restringe a esas carpetas.
 */
interface LibraryFolderRepository {

    /** URIs de las carpetas seleccionadas, como flujo observable. */
    fun observeSelectedFolders(): Flow<Set<String>>

    /** URIs de las carpetas seleccionadas (consulta puntual). */
    suspend fun selectedFolders(): Set<String>

    /** Agrega una carpeta (por su tree URI). */
    suspend fun addFolder(treeUri: String)

    /** Quita una carpeta (por su tree URI). */
    suspend fun removeFolder(treeUri: String)
}
