package com.auralis.player.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.auralis.player.data.database.AppDatabase
import com.auralis.player.data.database.entity.SongEntity
import com.auralis.player.domain.model.PlaylistId
import com.auralis.player.domain.model.PlaylistSong
import com.auralis.player.domain.model.SongId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomPlaylistRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: RoomPlaylistRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = RoomPlaylistRepository(db, db.playlistDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `crear playlist genera id y es observable`() = runTest {
        val id = repository.createPlaylist("Mi lista", description = "desc")

        val created = repository.getPlaylist(id)
        assertTrue(created != null)
        assertEquals("Mi lista", created?.name)
        assertEquals("desc", created?.description)
        assertEquals(listOf(id.value), repository.observePlaylists().first().map { it.id.value })
    }

    @Test
    fun `agregar canciones conserva el orden de insercion`() = runTest {
        val pid = repository.createPlaylist("Lista")
        repository.addSong(pid, SongId("s1"))
        repository.addSong(pid, SongId("s2"))
        repository.addSong(pid, SongId("s3"))

        assertEquals(
            listOf("s1", "s2", "s3"),
            repository.getSongs(pid).map { it.songId.value },
        )
    }

    @Test
    fun `mover cancion reasigna posiciones sin huecos`() = runTest {
        val pid = repository.createPlaylist("Lista")
        listOf("s1", "s2", "s3").forEach { repository.addSong(pid, SongId(it)) }

        repository.moveSong(pid, SongId("s3"), newPosition = 0)

        assertEquals(
            listOf("s3", "s1", "s2"),
            repository.getSongs(pid).sortedBy { it.position }.map { it.songId.value },
        )
    }

    @Test
    fun `quitar cancion compacta posiciones`() = runTest {
        val pid = repository.createPlaylist("Lista")
        listOf("s1", "s2", "s3").forEach { repository.addSong(pid, SongId(it)) }

        repository.removeSong(pid, SongId("s2"))

        val remaining = repository.getSongs(pid)
        assertEquals(listOf("s1", "s3"), remaining.map { it.songId.value })
        assertEquals(listOf(0, 1), remaining.map { it.position })
    }

    @Test
    fun `mover cancion inexistente no altera la lista`() = runTest {
        val pid = repository.createPlaylist("Lista")
        repository.addSong(pid, SongId("s1"))

        repository.moveSong(pid, SongId("fantasma"), newPosition = 0)

        assertEquals(
            listOf(PlaylistSong(pid, SongId("s1"), 0)),
            repository.getSongs(pid),
        )
    }

    @Test
    fun `renombrar y eliminar`() = runTest {
        val pid: PlaylistId = repository.createPlaylist("Viejo")

        repository.renamePlaylist(pid, "Nuevo")
        assertEquals("Nuevo", repository.getPlaylist(pid)?.name)

        repository.deletePlaylist(pid)
        assertNull(repository.getPlaylist(pid))
        assertTrue(repository.getSongs(pid).isEmpty())
    }

    @Test
    fun `agregar cancion duplicada no rompe el orden`() = runTest {
        val pid = repository.createPlaylist("Lista")
        repository.addSong(pid, SongId("s1"))
        repository.addSong(pid, SongId("s2"))

        repository.addSong(pid, SongId("s1"))

        val songs = repository.getSongs(pid).sortedBy { it.position }
        assertEquals(listOf(0, 1), songs.map { it.position })
        assertEquals(listOf("s1", "s2"), songs.map { it.songId.value })
        assertFalse(songs.isEmpty())
    }

    @Test
    fun `resumen calcula canciones visibles, duracion y portada`() = runTest {
        val pid = repository.createPlaylist("Lista")
        insertSong("s1", "Uno", 60_000, coverUri = "content://cover/1")
        insertSong("s2", "Dos", 120_000)
        insertSong("s3", "Oculta", 999_000, isHidden = true)
        listOf("s1", "s2", "s3").forEach { repository.addSong(pid, SongId(it)) }

        val summary = repository.observePlaylistSummaries().first().first { it.id == pid }

        assertEquals("Lista", summary.name)
        assertEquals(2, summary.songCount)
        assertEquals(180_000L, summary.totalDurationMs)
        assertEquals("content://cover/1", summary.coverReference)
    }

    @Test
    fun `resumen de playlist vacia cuenta cero`() = runTest {
        val pid = repository.createPlaylist("Vacía")
        val summary = repository.observePlaylistSummaries().first().first { it.id == pid }

        assertEquals(0, summary.songCount)
        assertEquals(0L, summary.totalDurationMs)
        assertNull(summary.coverReference)
    }

    @Test
    fun `editar descripcion persiste y admite vacio`() = runTest {
        val pid = repository.createPlaylist("Lista", description = "vieja")

        repository.updateDescription(pid, "nueva")
        assertEquals("nueva", repository.getPlaylist(pid)?.description)

        repository.updateDescription(pid, null)
        assertNull(repository.getPlaylist(pid)?.description)
    }

    private suspend fun insertSong(
        id: String,
        title: String,
        durationMs: Long?,
        coverUri: String? = null,
        isHidden: Boolean = false,
    ) {
        db.libraryDao().upsert(
            SongEntity(
                id = id,
                mediaStoreId = id.hashCode().toLong(),
                uri = "content://song/$id",
                title = title,
                artist = "Artista",
                album = "Álbum",
                albumArtist = "Artista",
                genre = null,
                year = null,
                trackNumber = null,
                discNumber = null,
                durationMs = durationMs,
                dateAddedEpochMs = null,
                dateModifiedEpochMs = null,
                mimeType = null,
                sizeBytes = null,
                path = null,
                playCount = 0,
                lastPlayedAtEpochMs = null,
                isFavorite = false,
                isHidden = isHidden,
                coverUri = coverUri,
            )
        )
    }
}
