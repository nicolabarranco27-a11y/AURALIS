// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.domain.repository

import com.auralis.player.domain.model.Album
import com.auralis.player.domain.model.Artist
import com.auralis.player.domain.model.Genre
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {

    suspend fun getSongs(): List<Song>

    suspend fun getAlbums(): List<Album>

    suspend fun getArtists(): List<Artist>

    suspend fun getGenres(): List<Genre>

    fun observeSongs(): Flow<List<Song>>

    fun observeAlbums(): Flow<List<Album>>

    fun observeArtists(): Flow<List<Artist>>

    fun observeGenres(): Flow<List<Genre>>

    fun observeSongsByAlbum(albumTitle: String, artist: String?): Flow<List<Song>>

    fun observeSongsByArtist(artistName: String): Flow<List<Song>>

    fun observeAlbumsByArtist(artistName: String): Flow<List<Album>>

    suspend fun searchSongs(query: String): List<Song>

    /** Canciones ocultas de la biblioteca (reversibles por el usuario). */
    fun observeHiddenSongs(): Flow<List<Song>>

    /** Oculta o restaura una cancion de la biblioteca. Reversible y no borra el archivo. */
    suspend fun setSongHidden(songId: SongId, hidden: Boolean)

    /** Registra una reproduccion de la cancion (para "mas populares"). */
    suspend fun registerPlay(songId: SongId, playedAtEpochMs: Long)

    /** Emite una senal cada vez que la biblioteca cambia. */
    fun observeLibraryChanges(): Flow<Unit>
}
