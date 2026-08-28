package com.auralis.player.data.database.mapper

import com.auralis.player.data.database.entity.PlaylistEntity
import com.auralis.player.data.database.entity.PlaylistSongEntity
import com.auralis.player.data.database.entity.SongEntity
import com.auralis.player.domain.model.Playlist
import com.auralis.player.domain.model.PlaylistId
import com.auralis.player.domain.model.PlaylistSong
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class MappersTest {

    private val epoch = Instant.ofEpochMilli(1_700_000_000_000L)

    private fun fullEntity() = SongEntity(
        id = "song-1",
        mediaStoreId = 42L,
        uri = "content://media/audio/42",
        title = "Tema",
        artist = "Artista",
        album = "Album",
        albumArtist = "Otro Artista",
        genre = "Rock",
        year = 2024,
        trackNumber = 3,
        discNumber = 1,
        durationMs = 210_000L,
        dateAddedEpochMs = epoch.toEpochMilli(),
        dateModifiedEpochMs = null,
        mimeType = "audio/mpeg",
        sizeBytes = 5_000_000L,
        path = "/music/tema.mp3",
        playCount = 7L,
        lastPlayedAtEpochMs = epoch.toEpochMilli(),
        isFavorite = true,
    )

    @Test
    fun `entity a domain conserva todos los campos`() {
        val song = fullEntity().toDomain()

        assertEquals("song-1", song.id.value)
        assertEquals("content://media/audio/42", song.sourceUri)
        assertEquals("Tema", song.title)
        assertEquals("Artista", song.artist)
        assertEquals(2024, song.year)
        assertEquals(210_000L, song.durationMs)
        assertEquals(epoch, song.dateAdded)
        assertEquals(7L, song.playCount)
        assertEquals(true, song.isFavorite)
    }

    @Test
    fun `entity con metadata nula mapea a domain nulo`() {
        val entity = SongEntity(
            id = "song-2",
            mediaStoreId = 43L,
            uri = "file:///a.flac",
            title = "Minima",
            artist = null,
            album = null,
            albumArtist = null,
            genre = null,
            year = null,
            trackNumber = null,
            discNumber = null,
            durationMs = null,
            dateAddedEpochMs = null,
            dateModifiedEpochMs = null,
            mimeType = null,
            sizeBytes = null,
            path = null,
            playCount = 0L,
            lastPlayedAtEpochMs = null,
            isFavorite = false,
        )

        val song = entity.toDomain()

        assertNull(song.artist)
        assertNull(song.album)
        assertNull(song.albumArtist)
        assertNull(song.genre)
        assertNull(song.year)
        assertNull(song.trackNumber)
        assertNull(song.discNumber)
        assertNull(song.durationMs)
        assertNull(song.dateAdded)
        assertNull(song.lastPlayedAt)
    }

    @Test
    fun `domain a entity y vuelta es equivalente`() {
        val original = Song(
            id = SongId("roundtrip"),
            sourceUri = "content://media/audio/9",
            title = "Roundtrip",
            artist = "A",
            album = "B",
            year = 1999,
            durationMs = 100L,
        )
        val entity = original.toEntity(
            mediaStoreId = 9L,
            mimeType = "audio/flac",
            sizeBytes = 1L,
            path = "/m/9.flac",
        )
        val restored = entity.toDomain()

        assertEquals(original, restored.copy(isFavorite = false))
        assertEquals("audio/flac", entity.mimeType)
        assertEquals("/m/9.flac", entity.path)
    }

    @Test
    fun `playlist y playlistSong mapean en ambas direcciones`() {
        val instant = Instant.ofEpochMilli(123L)
        val playlist = Playlist(
            id = PlaylistId("p-1"),
            name = "Lista",
            description = null,
            createdAt = instant,
            updatedAt = instant,
            isSystemPlaylist = false,
        ).toEntity().toDomain()

        assertEquals("p-1", playlist.id.value)
        assertEquals(instant, playlist.createdAt)

        val relation = PlaylistSong(
            playlistId = PlaylistId("p-1"),
            songId = SongId("s-1"),
            position = 4,
        ).toEntity()

        assertEquals(
            PlaylistSongEntity(playlistId = "p-1", songId = "s-1", position = 4),
            relation,
        )
    }
}
