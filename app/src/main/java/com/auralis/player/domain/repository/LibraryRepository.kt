package com.auralis.player.domain.repository

import com.auralis.player.domain.model.Album
import com.auralis.player.domain.model.Artist
import com.auralis.player.domain.model.Genre
import com.auralis.player.domain.model.Song
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

    /** Emite una senal cada vez que la biblioteca cambia. */
    fun observeLibraryChanges(): Flow<Unit>
}
