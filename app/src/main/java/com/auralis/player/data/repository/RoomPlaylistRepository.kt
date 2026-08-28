package com.auralis.player.data.repository

import androidx.room.withTransaction
import com.auralis.player.data.database.AppDatabase
import com.auralis.player.data.database.dao.PlaylistDao
import com.auralis.player.data.database.mapper.toDomain
import com.auralis.player.data.database.mapper.toEntity
import com.auralis.player.domain.model.Playlist
import com.auralis.player.domain.model.PlaylistId
import com.auralis.player.domain.model.PlaylistSong
import com.auralis.player.domain.model.SongId
import com.auralis.player.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomPlaylistRepository @Inject constructor(
    private val database: AppDatabase,
    private val playlistDao: PlaylistDao,
) : PlaylistRepository {

    override fun observePlaylists(): Flow<List<Playlist>> =
        playlistDao.observePlaylists().map { list -> list.map { it.toDomain() } }

    override suspend fun getPlaylist(id: PlaylistId): Playlist? =
        playlistDao.getPlaylist(id.value)?.toDomain()

    override suspend fun getSongs(id: PlaylistId): List<PlaylistSong> =
        playlistDao.getSongs(id.value).map { it.toDomain() }

    override fun observeSongs(id: PlaylistId): Flow<List<com.auralis.player.domain.model.Song>> =
        playlistDao.observePlaylistSongs(id.value).map { list -> list.map { it.toDomain() } }

    override suspend fun createPlaylist(name: String, description: String?): PlaylistId {
        val id = PlaylistId(UUID.randomUUID().toString())
        val now = Instant.now().toEpochMilli()
        playlistDao.insertPlaylist(
            Playlist(
                id = id,
                name = name,
                description = description,
                createdAt = Instant.ofEpochMilli(now),
                updatedAt = Instant.ofEpochMilli(now),
            ).toEntity(),
        )
        return id
    }

    override suspend fun renamePlaylist(id: PlaylistId, name: String) {
        playlistDao.renamePlaylist(id.value, name, Instant.now().toEpochMilli())
    }

    override suspend fun deletePlaylist(id: PlaylistId) {
        playlistDao.deletePlaylist(id.value)
    }

    override suspend fun addSong(playlistId: PlaylistId, songId: SongId) {
        val position = (playlistDao.getMaxPosition(playlistId.value) ?: -1) + 1
        playlistDao.insertSong(
            PlaylistSong(playlistId, songId, position).toEntity(),
        )
    }

    override suspend fun removeSong(playlistId: PlaylistId, songId: SongId) {
        database.withTransaction {
            playlistDao.deleteSong(playlistId.value, songId.value)
            compactPositions(playlistId)
        }
    }

    override suspend fun moveSong(playlistId: PlaylistId, songId: SongId, newPosition: Int) {
        database.withTransaction {
            val songs = playlistDao.getSongs(playlistId.value)
            val current = songs.firstOrNull { it.songId == songId.value } ?: return@withTransaction
            val fromIndex = songs.indexOf(current)

            val reordered = songs.toMutableList().apply {
                removeAt(fromIndex)
                add(newPosition.coerceIn(0, lastIndex + 1), current)
            }

            playlistDao.replaceSongs(
                reordered.mapIndexed { index, item ->
                    item.copy(position = index)
                },
            )
        }
    }

    private suspend fun compactPositions(playlistId: PlaylistId) {
        val remaining = playlistDao.getSongs(playlistId.value)
        if (remaining.map { it.position } != remaining.indices.toList()) {
            playlistDao.replaceSongs(
                remaining.mapIndexed { index, item -> item.copy(position = index) },
            )
        }
    }
}
