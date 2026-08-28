package com.auralis.player.data.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.auralis.player.data.database.AppDatabase
import com.auralis.player.data.database.entity.PlaylistEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PlaylistDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: PlaylistDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.playlistDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun playlist(id: String) = PlaylistEntity(
        id = id,
        name = "Playlist $id",
        description = null,
        createdAtEpochMs = 1L,
        updatedAtEpochMs = 1L,
        isSystemPlaylist = false,
    )

    private fun songEntry(playlistId: String, songId: String, position: Int) =
        com.auralis.player.data.database.entity.PlaylistSongEntity(
            playlistId = playlistId,
            songId = songId,
            position = position,
        )

    @Test
    fun `insertar obtener y renombrar playlist`() = runTest {
        dao.insertPlaylist(playlist("p1"))

        assertEquals("p1", dao.getPlaylist("p1")?.id)

        dao.renamePlaylist("p1", "Nuevo nombre", updatedAtEpochMs = 99L)

        val renamed = dao.getPlaylist("p1")!!
        assertEquals("Nuevo nombre", renamed.name)
        assertEquals(99L, renamed.updatedAtEpochMs)
    }

    @Test
    fun `eliminar playlist elimina sus canciones por cascade`() = runTest {
        dao.insertPlaylist(playlist("p1"))
        dao.insertSong(songEntry("p1", "s1", 0))
        dao.insertSong(songEntry("p1", "s2", 1))

        dao.deletePlaylist("p1")

        assertNull(dao.getPlaylist("p1"))
        assertTrue(dao.getSongs("p1").isEmpty())
    }

    @Test
    fun `obtener canciones ordenadas por posicion`() = runTest {
        dao.insertPlaylist(playlist("p1"))
        dao.insertSong(songEntry("p1", "b", 1))
        dao.insertSong(songEntry("p1", "a", 0))
        dao.insertSong(songEntry("p1", "c", 2))

        val order = dao.getSongs("p1").map { it.songId }

        assertEquals(listOf("a", "b", "c"), order)
    }

    @Test
    fun `maxPosition devuelve la posicion mas alta o null`() = runTest {
        dao.insertPlaylist(playlist("p1"))

        assertNull(dao.getMaxPosition("p1"))

        dao.insertSong(songEntry("p1", "a", 0))
        dao.insertSong(songEntry("p1", "b", 5))

        assertEquals(5, dao.getMaxPosition("p1"))
    }

    @Test
    fun `replaceSongs actualiza posiciones`() = runTest {
        dao.insertPlaylist(playlist("p1"))
        dao.insertSong(songEntry("p1", "a", 0))
        dao.insertSong(songEntry("p1", "b", 1))

        dao.replaceSongs(
            listOf(
                songEntry("p1", "a", 1),
                songEntry("p1", "b", 0),
            ),
        )

        assertEquals(listOf("b", "a"), dao.getSongs("p1").map { it.songId })
    }
}
