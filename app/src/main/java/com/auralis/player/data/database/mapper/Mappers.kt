package com.auralis.player.data.database.mapper

import com.auralis.player.data.database.entity.AlbumEntity
import com.auralis.player.data.database.entity.ArtistEntity
import com.auralis.player.data.database.entity.GenreEntity
import com.auralis.player.data.database.entity.PlaylistEntity
import com.auralis.player.data.database.entity.PlaylistSongEntity
import com.auralis.player.data.database.entity.SongEntity
import com.auralis.player.domain.model.Album
import com.auralis.player.domain.model.AlbumId
import com.auralis.player.domain.model.Artist
import com.auralis.player.domain.model.ArtistId
import com.auralis.player.domain.model.Genre
import com.auralis.player.domain.model.GenreId
import com.auralis.player.domain.model.Playlist
import com.auralis.player.domain.model.PlaylistId
import com.auralis.player.domain.model.PlaylistSong
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import java.time.Instant

fun SongEntity.toDomain(): Song = Song(
    id = SongId(id),
    sourceUri = uri,
    title = title,
    artist = artist,
    album = album,
    albumArtist = albumArtist,
    genre = genre,
    year = year,
    trackNumber = trackNumber,
    discNumber = discNumber,
    durationMs = durationMs,
    dateAdded = dateAddedEpochMs?.let(Instant::ofEpochMilli),
    playCount = playCount,
    lastPlayedAt = lastPlayedAtEpochMs?.let(Instant::ofEpochMilli),
    isFavorite = isFavorite,
    coverReference = coverUri,
)

fun Song.toEntity(
    mediaStoreId: Long,
    mimeType: String?,
    sizeBytes: Long?,
    path: String?,
    dateModifiedEpochMs: Long? = null,
): SongEntity = SongEntity(
    id = id.value,
    mediaStoreId = mediaStoreId,
    uri = sourceUri,
    title = title,
    artist = artist,
    album = album,
    albumArtist = albumArtist,
    genre = genre,
    year = year,
    trackNumber = trackNumber,
    discNumber = discNumber,
    durationMs = durationMs,
    dateAddedEpochMs = dateAdded?.toEpochMilli(),
    dateModifiedEpochMs = dateModifiedEpochMs,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    path = path,
    playCount = playCount,
    lastPlayedAtEpochMs = lastPlayedAt?.toEpochMilli(),
    isFavorite = isFavorite,
    coverUri = coverReference,
    isAvailable = true,
)

fun AlbumEntity.toDomain(): Album = Album(
    id = AlbumId(id),
    title = title,
    artist = artist,
    year = year,
    coverReference = coverUri,
    songCount = songCount,
)

fun ArtistEntity.toDomain(): Artist = Artist(
    id = ArtistId(id),
    name = name,
    songCount = songCount,
    albumCount = albumCount,
)

fun GenreEntity.toDomain(): Genre = Genre(
    id = GenreId(id),
    name = name,
    songCount = songCount,
)

fun PlaylistEntity.toDomain(): Playlist = Playlist(
    id = PlaylistId(id),
    name = name,
    description = description,
    createdAt = Instant.ofEpochMilli(createdAtEpochMs),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMs),
    isSystemPlaylist = isSystemPlaylist,
)

fun Playlist.toEntity(): PlaylistEntity = PlaylistEntity(
    id = id.value,
    name = name,
    description = description,
    createdAtEpochMs = createdAt.toEpochMilli(),
    updatedAtEpochMs = updatedAt.toEpochMilli(),
    isSystemPlaylist = isSystemPlaylist,
)

fun PlaylistSongEntity.toDomain(): PlaylistSong = PlaylistSong(
    playlistId = PlaylistId(playlistId),
    songId = SongId(songId),
    position = position,
)

fun PlaylistSong.toEntity(): PlaylistSongEntity = PlaylistSongEntity(
    playlistId = playlistId.value,
    songId = songId.value,
    position = position,
)
