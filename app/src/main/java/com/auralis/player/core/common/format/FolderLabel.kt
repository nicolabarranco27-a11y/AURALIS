// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.core.common.format

import android.net.Uri

/**
 * Nombre legible de una carpeta elegida con el selector de documentos (SAF).
 *
 * El URI de arbol tiene la forma
 * `content://<autoridad>/tree/<volumen>:<ruta relativa>`. Mostramos solo la
 * ruta relativa, sin el identificador de volumen.
 */
fun folderDisplayName(treeUri: String): String {
    val decoded = runCatching { Uri.decode(treeUri) }.getOrDefault(treeUri)
    val documentId = decoded.substringAfterLast("/tree/", decoded)
    val relative = documentId.substringAfter(':', documentId).trim('/')
    return if (relative.isEmpty()) "Almacenamiento interno" else relative
}
