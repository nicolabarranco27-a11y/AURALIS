package com.auralis.player.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.auralis.player.data.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {

    @Query("SELECT * FROM songs WHERE isAvailable = 1 ORDER BY title COLLATE NOCASE")
    fun observeSongs(): Flow<List<SongEntity>>

    @Query("""
        SELECT 
            (album || '|' || COALESCE(artist, '')) as id,
            album as title,
            artist,
            MAX(year) as year,
            COUNT(*) as songCount
        FROM songs
        WHERE isAvailable = 1 AND album IS NOT NULL
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
            COUNT(*) as songCount
        FROM songs
        WHERE isAvailable = 1 AND album IS NOT NULL
        GROUP BY album, artist
        ORDER BY album COLLATE NOCASE
    """)
    suspend fun getAlbums(): List<com.auralis.player.data.database.entity.AlbumEntity>

    @Query("""
        SELECT * FROM songs
        WHERE isAvailable = 1 
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
        WHERE isAvailable = 1 AND artist IS NOT NULL
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
        WHERE isAvailable = 1 AND artist IS NOT NULL
        GROUP BY artist
        ORDER BY artist COLLATE NOCASE
    """)
    suspend fun getArtists(): List<com.auralis.player.data.database.entity.ArtistEntity>

    @Query("""
        SELECT * FROM songs
        WHERE isAvailable = 1 AND artist = :artistName
        ORDER BY album COLLATE NOCASE, discNumber ASC, trackNumber ASC, title COLLATE NOCASE
    """)
    fun observeSongsByArtist(artistName: String): Flow<List<SongEntity>>

    @Query("""
        SELECT 
            (album || '|' || COALESCE(artist, '')) as id,
            album as title,
            artist,
            MAX(year) as year,
            COUNT(*) as songCount
        FROM songs
        WHERE isAvailable = 1 AND artist = :artistName AND album IS NOT NULL
        GROUP BY album
        ORDER BY album COLLATE NOCASE
    """)
    fun observeAlbumsByArtist(artistName: String): Flow<List<com.auralis.player.data.database.entity.AlbumEntity>>

    @Query("SELECT * FROM songs WHERE isAvailable = 1 ORDER BY title COLLATE NOCASE")
    suspend fun getSongs(): List<SongEntity>

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE")
    suspend fun getAllIncludingUnavailable(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getById(id: String): SongEntity?

    @Query(
        """
        SELECT * FROM songs
        WHERE isAvailable = 1
          AND (title LIKE '%' || :query || '%'
           OR artist LIKE '%' || :query || '%'
           OR album LIKE '%' || :query || '%')
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

    /** Eliminacion logica: conserva playlists, favoritos e historico. */
    @Query(
        """
        UPDATE songs SET isAvailable = 0
        WHERE isAvailable = 1 AND id NOT IN (:availableIds)
        """,
    )
    suspend fun markUnavailableExcept(availableIds: List<String>): Int
}
