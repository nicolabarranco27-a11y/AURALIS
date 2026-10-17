// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.domain.repository

import com.auralis.player.domain.model.Playlist
import com.auralis.player.domain.model.PlaylistId
import com.auralis.player.domain.model.PlaylistSong
import com.auralis.player.domain.model.PlaylistSummary
import com.auralis.player.domain.model.SongId
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {

    fun observePlaylists(): Flow<List<Playlist>>

    /** Playlists con estadisticas (numero de canciones visibles, duracion, portada). */
    fun observePlaylistSummaries(): Flow<List<PlaylistSummary>>

    suspend fun getPlaylist(id: PlaylistId): Playlist?

    suspend fun getSongs(id: PlaylistId): List<PlaylistSong>

    fun observeSongs(id: PlaylistId): Flow<List<com.auralis.player.domain.model.Song>>

    suspend fun createPlaylist(name: String, description: String? = null): PlaylistId

    suspend fun renamePlaylist(id: PlaylistId, name: String)

    suspend fun updateDescription(id: PlaylistId, description: String?)

    suspend fun deletePlaylist(id: PlaylistId)

    suspend fun addSong(playlistId: PlaylistId, songId: SongId)

    suspend fun removeSong(playlistId: PlaylistId, songId: SongId)

    /** Mueve [songId] a [newPosition] y compacta las posiciones restantes. */
    suspend fun moveSong(playlistId: PlaylistId, songId: SongId, newPosition: Int)
}
