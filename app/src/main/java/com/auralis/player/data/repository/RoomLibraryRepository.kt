package com.auralis.player.data.repository

import com.auralis.player.data.database.dao.LibraryDao
import com.auralis.player.data.database.mapper.toDomain
import com.auralis.player.domain.model.Album
import com.auralis.player.domain.model.Artist
import com.auralis.player.domain.model.Genre
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room como fuente de verdad del catalogo.
 * Albumes, artistas y generos se materializaran cuando exista el scanner;
 * mientras tanto las tablas permanecen vacias.
 */
@Singleton
class RoomLibraryRepository @Inject constructor(
    private val libraryDao: LibraryDao,
) : LibraryRepository {

    override suspend fun getSongs(): List<Song> =
        libraryDao.getSongs().map { it.toDomain() }

    override suspend fun getAlbums(): List<Album> =
        libraryDao.getAlbums().map { it.toDomain() }

    override suspend fun getArtists(): List<Artist> =
        libraryDao.getArtists().map { it.toDomain() }

    override suspend fun getGenres(): List<Genre> = emptyList()

    override fun observeSongs(): Flow<List<Song>> =
        libraryDao.observeSongs().map { songs -> songs.map { it.toDomain() } }

    override fun observeAlbums(): Flow<List<Album>> =
        libraryDao.observeAlbums().map { albums -> albums.map { it.toDomain() } }

    override fun observeArtists(): Flow<List<Artist>> =
        libraryDao.observeArtists().map { artists -> artists.map { it.toDomain() } }

    override fun observeGenres(): Flow<List<Genre>> =
        libraryDao.observeSongs().map { emptyList() }

    override fun observeSongsByAlbum(albumTitle: String, artist: String?): Flow<List<Song>> =
        libraryDao.observeSongsByAlbum(albumTitle, artist).map { songs -> songs.map { it.toDomain() } }

    override fun observeSongsByArtist(artistName: String): Flow<List<Song>> =
        libraryDao.observeSongsByArtist(artistName).map { songs -> songs.map { it.toDomain() } }

    override fun observeAlbumsByArtist(artistName: String): Flow<List<Album>> =
        libraryDao.observeAlbumsByArtist(artistName).map { albums -> albums.map { it.toDomain() } }

    override suspend fun searchSongs(query: String): List<Song> =
        if (query.isBlank()) {
            emptyList()
        } else {
            libraryDao.search(query.trim()).map { it.toDomain() }
        }

    /** Los observe* de Room ya emiten en cada cambio; esta senal es explicita. */
    override fun observeLibraryChanges(): Flow<Unit> =
        libraryDao.observeSongs().map { }
}
