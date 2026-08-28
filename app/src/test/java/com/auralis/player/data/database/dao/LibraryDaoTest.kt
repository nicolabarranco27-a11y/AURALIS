package com.auralis.player.data.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.auralis.player.data.database.AppDatabase
import com.auralis.player.data.database.entity.SongEntity
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
class LibraryDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun song(id: String, title: String, artist: String? = null) = SongEntity(
        id = id,
        mediaStoreId = id.hashCode().toLong(),
        uri = "content://media/audio/$id",
        title = title,
        artist = artist,
        album = null,
        albumArtist = null,
        genre = null,
        year = null,
        trackNumber = null,
        discNumber = null,
        durationMs = 100L,
        dateAddedEpochMs = 1L,
        dateModifiedEpochMs = null,
        mimeType = null,
        sizeBytes = null,
        path = null,
        playCount = 0L,
        lastPlayedAtEpochMs = null,
        isFavorite = false,
    )

    @Test
    fun `insertar y leer cancion`() = runTest {
        val dao = db.libraryDao()

        dao.upsert(song("s1", "Alpha", artist = "Artista"))

        val loaded = dao.getById("s1")
        assertTrue(loaded != null)
        assertEquals("Alpha", loaded?.title)
        assertEquals("Artista", loaded?.artist)
    }

    @Test
    fun `actualizar cancion existente`() = runTest {
        val dao = db.libraryDao()

        dao.upsert(song("s1", "Viejo"))
        dao.upsert(song("s1", "Nuevo"))

        assertEquals("Nuevo", dao.getById("s1")?.title)
        assertEquals(1, dao.getSongs().size)
    }

    @Test
    fun `buscar por titulo artista o album`() = runTest {
        val dao = db.libraryDao()

        dao.upsertAll(
            listOf(
                song("s1", "Nightcall", artist = "Kavinsky"),
                song("s2", "Other", artist = "Daft Punk"),
                song("s3", "Random", artist = "Alguien"),
            ),
        )

        assertEquals(listOf("s1"), dao.search("night").map { it.id })
        assertEquals(listOf("s2"), dao.search("daft").map { it.id })
        assertTrue(dao.search("inexistente").isEmpty())
    }

    @Test
    fun `eliminar por ids`() = runTest {
        val dao = db.libraryDao()

        dao.upsertAll(listOf(song("s1", "A"), song("s2", "B")))

        dao.deleteByIds(listOf("s1"))

        assertNull(dao.getById("s1"))
        assertEquals("s2", dao.getById("s2")?.id)
    }

    @Test
    fun `markUnavailableExcept oculta las canciones ausentes sin borrarlas`() = runTest {
        val dao = db.libraryDao()

        dao.upsertAll(listOf(song("s1", "A"), song("s2", "B"), song("s3", "C")))

        val removed = dao.markUnavailableExcept(listOf("s2"))

        assertEquals(2, removed)
        assertFalse(dao.getById("s1")!!.isAvailable)
        assertFalse(dao.getById("s3")!!.isAvailable)
        assertTrue(dao.getById("s2")!!.isAvailable)
        assertEquals(listOf("s2"), dao.getSongs().map { it.id })
        assertEquals(3, dao.getAllIncludingUnavailable().size)
    }

    @Test
    fun `upsertAll restaura disponibilidad`() = runTest {
        val dao = db.libraryDao()

        dao.upsert(song("s1", "A"))
        dao.markUnavailableExcept(emptyList())
        assertTrue(dao.getSongs().isEmpty())

        dao.upsertAll(listOf(song("s1", "A").copy(isAvailable = true)))

        assertEquals(listOf("s1"), dao.getSongs().map { it.id })
    }

    @Test
    fun `observeSongs emite ordenado por titulo`() = runTest {
        val dao = db.libraryDao()

        dao.upsertAll(listOf(song("s2", "Beta"), song("s1", "alpha")))

        val titles = dao.observeSongs().first().map { it.title }
        assertEquals(listOf("alpha", "Beta"), titles)
    }
}
