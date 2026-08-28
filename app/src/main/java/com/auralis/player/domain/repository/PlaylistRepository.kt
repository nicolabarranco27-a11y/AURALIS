package com.auralis.player.domain.repository

import com.auralis.player.domain.model.Playlist
import com.auralis.player.domain.model.PlaylistId
import com.auralis.player.domain.model.PlaylistSong
import com.auralis.player.domain.model.SongId
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {

    fun observePlaylists(): Flow<List<Playlist>>

    suspend fun getPlaylist(id: PlaylistId): Playlist?

    suspend fun getSongs(id: PlaylistId): List<PlaylistSong>

    fun observeSongs(id: PlaylistId): Flow<List<com.auralis.player.domain.model.Song>>

    suspend fun createPlaylist(name: String, description: String? = null): PlaylistId

    suspend fun renamePlaylist(id: PlaylistId, name: String)

    suspend fun deletePlaylist(id: PlaylistId)

    suspend fun addSong(playlistId: PlaylistId, songId: SongId)

    suspend fun removeSong(playlistId: PlaylistId, songId: SongId)

    /** Mueve [songId] a [newPosition] y compacta las posiciones restantes. */
    suspend fun moveSong(playlistId: PlaylistId, songId: SongId, newPosition: Int)
}
