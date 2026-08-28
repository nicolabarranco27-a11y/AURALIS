package com.auralis.player.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class DomainModelsTest {

    @Test
    fun `Song se crea con metadatos completos`() {
        val added = Instant.parse("2026-08-26T10:00:00Z")
        val song = Song(
            id = SongId("song-1"),
            sourceUri = "content://media/external/audio/1",
            title = "Tema",
            artist = "Artista",
            album = "Album",
            albumArtist = "Artista",
            genre = "Rock",
            year = 2024,
            trackNumber = 3,
            discNumber = 1,
            durationMs = 210_000L,
            dateAdded = added,
            playCount = 5L,
            lastPlayedAt = added,
            isFavorite = true,
            coverReference = "cover://1",
        )

        assertEquals("song-1", song.id.value)
        assertEquals("Tema", song.title)
        assertEquals(2024, song.year)
        assertTrue(song.isFavorite)
        assertEquals(5L, song.playCount)
    }

    @Test
    fun `Song permite metadata ausente sin valores falsos`() {
        val song = Song(
            id = SongId("song-2"),
            sourceUri = "file:///music/a.flac",
            title = "Sin datos",
        )

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
        assertNull(song.coverReference)
        assertFalse(song.isFavorite)
    }

    @Test
    fun `RepeatMode contiene exactamente OFF ONE y ALL`() {
        assertEquals(listOf(RepeatMode.OFF, RepeatMode.ONE, RepeatMode.ALL), RepeatMode.entries)
    }

    @Test
    fun `PlaylistSong conserva la posicion`() {
        val item = PlaylistSong(
            playlistId = PlaylistId("pl-1"),
            songId = SongId("song-9"),
            position = 7,
        )

        assertEquals(7, item.position)
        assertEquals("pl-1", item.playlistId.value)
        assertEquals("song-9", item.songId.value)
    }

    @Test
    fun `QueueItem envuelve una cancion con identidad propia`() {
        val song = Song(id = SongId("s"), sourceUri = "uri", title = "t")
        val first = QueueItem(uid = "q-0", song = song)
        val second = QueueItem(uid = "q-1", song = song)

        assertEquals(song, first.song)
        assertTrue(first != second)
    }

    @Test
    fun `PlaybackState por defecto es idle`() {
        val state = PlaybackState.IDLE

        assertNull(state.currentSong)
        assertFalse(state.isPlaying)
        assertEquals(0L, state.positionMs)
        assertNull(state.durationMs)
        assertFalse(state.shuffleEnabled)
        assertEquals(RepeatMode.OFF, state.repeatMode)
    }

    @Test
    fun `PlaybackState representa reproduccion activa`() {
        val song = Song(id = SongId("s"), sourceUri = "uri", title = "t")
        val state = PlaybackState(
            currentSong = song,
            isPlaying = true,
            positionMs = 30_000L,
            durationMs = 200_000L,
            shuffleEnabled = true,
            repeatMode = RepeatMode.ONE,
        )

        assertEquals(song, state.currentSong)
        assertTrue(state.isPlaying)
        assertEquals(30_000L, state.positionMs)
        assertEquals(200_000L, state.durationMs)
        assertTrue(state.shuffleEnabled)
        assertEquals(RepeatMode.ONE, state.repeatMode)
    }

    @Test
    fun `Album Artist y Genre exponen conteos`() {
        val album = Album(
            id = AlbumId("a"),
            title = "Album",
            artist = "Artista",
            year = 2020,
            songCount = 12,
        )
        val artist = Artist(id = ArtistId("ar"), name = "Artista", songCount = 40, albumCount = 3)
        val genre = Genre(id = GenreId("g"), name = "Jazz", songCount = 15)

        assertEquals(12, album.songCount)
        assertEquals(3, artist.albumCount)
        assertEquals(15, genre.songCount)
        assertNull(album.coverReference)
    }

    @Test
    fun `Playlist modela fechas y flag de sistema`() {
        val now = Instant.parse("2026-08-26T00:00:00Z")
        val playlist = Playlist(
            id = PlaylistId("p"),
            name = "Favoritas",
            description = null,
            createdAt = now,
            updatedAt = now,
            isSystemPlaylist = true,
        )

        assertEquals(now, playlist.createdAt)
        assertEquals(now, playlist.updatedAt)
        assertTrue(playlist.isSystemPlaylist)
        assertNull(playlist.description)
    }
}
