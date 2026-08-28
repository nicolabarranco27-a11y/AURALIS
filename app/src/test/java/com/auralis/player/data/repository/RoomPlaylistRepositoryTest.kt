package com.auralis.player.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.auralis.player.data.database.AppDatabase
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
}
