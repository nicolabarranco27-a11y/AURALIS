// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.core.common.folder

import com.auralis.player.domain.model.Song

/**
 * Resumen de una carpeta elegida por el usuario (SAF) para mostrarla como una
 * entrada tipo playlist en Inicio. La identidad es su tree URI.
 */
data class MusicLibraryFolder(
    val uri: String,
    val songCount: Int,
    val coverReference: String?,
)

/**
 * Canciones cuyo `sourceFolderUri` coincide con [folderUri]. `null` devuelve la
 * biblioteca completa (todas las carpetas seleccionadas).
 */
fun songsInSelectedFolder(songs: List<Song>, folderUri: String?): List<Song> =
    if (folderUri == null) songs else songs.filter { it.sourceFolderUri == folderUri }

/**
 * Resumen de cada carpeta seleccionada en [selectedUris], con su numero de
 * canciones y una portada representativa. Conserva solo las carpetas que
 * existen actualmente en [selectedUris].
 */
fun libraryFolders(
    songs: List<Song>,
    selectedUris: Collection<String>,
): List<MusicLibraryFolder> {
    val byFolder = songs
        .mapNotNull { song -> song.sourceFolderUri?.let { it to song } }
        .groupBy({ it.first }, { it.second })

    return selectedUris.map { uri ->
        val folderSongs = byFolder[uri].orEmpty()
        MusicLibraryFolder(
            uri = uri,
            songCount = folderSongs.size,
            coverReference = folderSongs.firstOrNull()?.coverReference,
        )
    }
}
