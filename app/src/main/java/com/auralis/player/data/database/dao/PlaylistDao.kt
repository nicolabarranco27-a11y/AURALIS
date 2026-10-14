package com.auralis.player.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.auralis.player.data.database.entity.PlaylistEntity
import com.auralis.player.data.database.entity.PlaylistSongEntity
import kotlinx.coroutines.flow.Flow

/** Proyeccion con estadisticas por playlist (canciones visibles y duracion). */
data class PlaylistSummaryRow(
    val id: String,
    val name: String,
    val description: String?,
    val songCount: Int,
    val totalDurationMs: Long,
    val coverUri: String?,
)

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY name COLLATE NOCASE")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Query(
        """
        SELECT p.id AS id,
               p.name AS name,
               p.description AS description,
               (SELECT COUNT(*) FROM playlist_songs ps
                    JOIN songs s ON s.id = ps.songId
                    WHERE ps.playlistId = p.id AND s.isHidden = 0) AS songCount,
               (SELECT COALESCE(SUM(s.durationMs), 0) FROM playlist_songs ps
                    JOIN songs s ON s.id = ps.songId
                    WHERE ps.playlistId = p.id AND s.isHidden = 0) AS totalDurationMs,
               (SELECT s.coverUri FROM playlist_songs ps
                    JOIN songs s ON s.id = ps.songId
                    WHERE ps.playlistId = p.id AND s.isHidden = 0
                    ORDER BY ps.position LIMIT 1) AS coverUri
        FROM playlists p
        ORDER BY p.name COLLATE NOCASE
        """
    )
    fun observePlaylistSummaries(): Flow<List<PlaylistSummaryRow>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylist(id: String): PlaylistEntity?

    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("UPDATE playlists SET name = :name, updatedAtEpochMs = :updatedAtEpochMs WHERE id = :id")
    suspend fun renamePlaylist(id: String, name: String, updatedAtEpochMs: Long)

    @Query("UPDATE playlists SET description = :description, updatedAtEpochMs = :updatedAtEpochMs WHERE id = :id")
    suspend fun updateDescription(id: String, description: String?, updatedAtEpochMs: Long)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: String)

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY position")
    suspend fun getSongs(playlistId: String): List<PlaylistSongEntity>

    @Query("SELECT MAX(position) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getMaxPosition(playlistId: String): Int?

    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    suspend fun insertSong(song: PlaylistSongEntity)

    @Update
    suspend fun updateSong(song: PlaylistSongEntity)

    @Transaction
    @Query("""
        SELECT s.* FROM songs s
        JOIN playlist_songs ps ON s.id = ps.songId
        WHERE ps.playlistId = :playlistId AND s.isHidden = 0
        ORDER BY ps.position
    """)
    fun observePlaylistSongs(playlistId: String): Flow<List<com.auralis.player.data.database.entity.SongEntity>>

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deleteSong(playlistId: String, songId: String)

    @Transaction
    suspend fun replaceSongs(items: List<PlaylistSongEntity>) {
        for (item in items) {
            updateSong(item)
        }
    }
}
