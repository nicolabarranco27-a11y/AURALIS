// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.auralis.player.data.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {

    @Query("SELECT * FROM songs WHERE isAvailable = 1 AND isHidden = 0 ORDER BY title COLLATE NOCASE")
    fun observeSongs(): Flow<List<SongEntity>>

    @Query("""
        SELECT 
            (album || '|' || COALESCE(artist, '')) as id,
            album as title,
            artist,
            MAX(year) as year,
            MAX(coverUri) as coverUri,
            COUNT(*) as songCount
        FROM songs
        WHERE isAvailable = 1 AND isHidden = 0 AND album IS NOT NULL
        GROUP BY album, artist
        ORDER BY album COLLATE NOCASE
    """)
    fun observeAlbums(): Flow<List<com.auralis.player.data.database.entity.AlbumEntity>>

    @Query("""
        SELECT 
            (album || '|' || COALESCE(artist, '')) as id,
            album as title,
            artist,
            MAX(year) as year,
            MAX(coverUri) as coverUri,
            COUNT(*) as songCount
        FROM songs
        WHERE isAvailable = 1 AND isHidden = 0 AND album IS NOT NULL
        GROUP BY album, artist
        ORDER BY album COLLATE NOCASE
    """)
    suspend fun getAlbums(): List<com.auralis.player.data.database.entity.AlbumEntity>

    @Query("""
        SELECT * FROM songs
        WHERE isAvailable = 1 AND isHidden = 0
          AND album = :albumTitle 
          AND (artist = :artist OR (artist IS NULL AND :artist IS NULL))
        ORDER BY discNumber ASC, trackNumber ASC, title COLLATE NOCASE
    """)
    fun observeSongsByAlbum(albumTitle: String, artist: String?): Flow<List<SongEntity>>

    @Query("""
        SELECT 
            artist as id,
            artist as name,
            COUNT(*) as songCount,
            COUNT(DISTINCT album) as albumCount
        FROM songs
        WHERE isAvailable = 1 AND isHidden = 0 AND artist IS NOT NULL
        GROUP BY artist
        ORDER BY artist COLLATE NOCASE
    """)
    fun observeArtists(): Flow<List<com.auralis.player.data.database.entity.ArtistEntity>>

    @Query("""
        SELECT 
            artist as id,
            artist as name,
            COUNT(*) as songCount,
            COUNT(DISTINCT album) as albumCount
        FROM songs
        WHERE isAvailable = 1 AND isHidden = 0 AND artist IS NOT NULL
        GROUP BY artist
        ORDER BY artist COLLATE NOCASE
    """)
    suspend fun getArtists(): List<com.auralis.player.data.database.entity.ArtistEntity>

    @Query("""
        SELECT * FROM songs
        WHERE isAvailable = 1 AND isHidden = 0 AND artist = :artistName
        ORDER BY album COLLATE NOCASE, discNumber ASC, trackNumber ASC, title COLLATE NOCASE
    """)
    fun observeSongsByArtist(artistName: String): Flow<List<SongEntity>>

    @Query("""
        SELECT 
            (album || '|' || COALESCE(artist, '')) as id,
            album as title,
            artist,
            MAX(year) as year,
            MAX(coverUri) as coverUri,
            COUNT(*) as songCount
        FROM songs
        WHERE isAvailable = 1 AND isHidden = 0 AND artist = :artistName AND album IS NOT NULL
        GROUP BY album
        ORDER BY album COLLATE NOCASE
    """)
    fun observeAlbumsByArtist(artistName: String): Flow<List<com.auralis.player.data.database.entity.AlbumEntity>>

    @Query("SELECT * FROM songs WHERE isAvailable = 1 AND isHidden = 0 ORDER BY title COLLATE NOCASE")
    suspend fun getSongs(): List<SongEntity>

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE")
    suspend fun getAllIncludingUnavailable(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getById(id: String): SongEntity?

    @Query("""
        SELECT * FROM songs
        WHERE isAvailable = 1 AND isHidden = 1
        ORDER BY title COLLATE NOCASE
    """)
    fun observeHiddenSongs(): Flow<List<SongEntity>>

    @Query("""
        SELECT * FROM songs
        WHERE isAvailable = 1 AND isHidden = 1
        ORDER BY title COLLATE NOCASE
    """)
    suspend fun getHiddenSongs(): List<SongEntity>

    /**
     * Busqueda insensible a mayusculas y acentos sobre la clave premormalizada
     * [SongEntity.searchKey]. La consulta llega ya normalizada desde la capa de
     * aplicacion (ver `normalizeForSearch`), de modo que solo se evalua un
     * unico LIKE por fila.
     */
    @Query(
        """
        SELECT * FROM songs
        WHERE isAvailable = 1 AND isHidden = 0
          AND searchKey LIKE '%' || :query || '%'
        ORDER BY title COLLATE NOCASE
        """,
    )
    suspend fun search(query: String): List<SongEntity>

    @Upsert
    suspend fun upsert(song: SongEntity)

    @Upsert
    suspend fun upsertAll(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("UPDATE songs SET isHidden = :isHidden WHERE id = :id")
    suspend fun setHidden(id: String, isHidden: Boolean)

    /** Registra una reproduccion: incrementa el contador y actualiza la fecha. */
    @Query(
        "UPDATE songs SET playCount = playCount + 1, lastPlayedAtEpochMs = :playedAtEpochMs " +
            "WHERE id = :id",
    )
    suspend fun registerPlay(id: String, playedAtEpochMs: Long)

    /**
     * Eliminacion logica por lotes: conserva playlists, favoritos e historico.
     * Se invoca con listas acotadas para no exceder el limite de variables
     * de SQLite (SQLITE_MAX_VARIABLE_NUMBER) en bibliotecas grandes.
     */
    @Query("UPDATE songs SET isAvailable = 0 WHERE isAvailable = 1 AND id IN (:ids)")
    suspend fun markUnavailable(ids: List<String>): Int
}
