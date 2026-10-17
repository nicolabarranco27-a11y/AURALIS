// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

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
        searchKey = com.auralis.player.data.database.mapper.buildSearchKey(title, artist, null),
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
    fun `buscar es insensible a acentos y mayusculas acentuadas`() = runTest {
        val dao = db.libraryDao()

        dao.upsertAll(
            listOf(
                song("s1", "Índigo", artist = "Camila"),
                song("s2", "Canción de Cuna"),
                song("s3", "Peña"),
            ),
        )

        assertEquals(listOf("s1"), dao.search("indigo").map { it.id })
        assertEquals(listOf("s2"), dao.search("cancion").map { it.id })
        assertEquals(listOf("s3"), dao.search("pena").map { it.id })
        assertEquals(listOf("s1"), dao.search("camila").map { it.id })
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
    fun `markUnavailable oculta las canciones indicadas sin borrarlas`() = runTest {
        val dao = db.libraryDao()

        dao.upsertAll(listOf(song("s1", "A"), song("s2", "B"), song("s3", "C")))

        val removed = dao.markUnavailable(listOf("s1", "s3"))

        assertEquals(2, removed)
        assertFalse(dao.getById("s1")!!.isAvailable)
        assertFalse(dao.getById("s3")!!.isAvailable)
        assertTrue(dao.getById("s2")!!.isAvailable)
        assertEquals(listOf("s2"), dao.getSongs().map { it.id })
        assertEquals(3, dao.getAllIncludingUnavailable().size)
    }

    @Test
    fun `markUnavailable no cuenta canciones ya no disponibles`() = runTest {
        val dao = db.libraryDao()

        dao.upsertAll(listOf(song("s1", "A"), song("s2", "B")))
        dao.markUnavailable(listOf("s1"))

        val removedAgain = dao.markUnavailable(listOf("s1"))

        assertEquals(0, removedAgain)
    }

    @Test
    fun `markUnavailable soporta listas grandes sin exceder variables sqlite`() = runTest {
        val dao = db.libraryDao()
        val ids = (1..1_200).map { "song$it" }
        dao.upsertAll(ids.mapIndexed { index, id -> song(id, "T$index") })

        val removed = ids.chunked(500).sumOf { dao.markUnavailable(it) }

        assertEquals(1_200, removed)
        assertTrue(dao.getSongs().isEmpty())
        assertEquals(1_200, dao.getAllIncludingUnavailable().size)
    }

    @Test
    fun `upsertAll restaura disponibilidad`() = runTest {
        val dao = db.libraryDao()

        dao.upsert(song("s1", "A"))
        dao.markUnavailable(listOf("s1"))
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

    @Test
    fun `setHidden oculta la cancion de las consultas publicas`() = runTest {
        val dao = db.libraryDao()

        dao.upsertAll(listOf(song("s1", "A"), song("s2", "B")))
        dao.setHidden("s1", true)

        assertEquals(listOf("s2"), dao.getSongs().map { it.id })
        assertEquals(listOf("s1"), dao.observeHiddenSongs().first().map { it.id })
        assertEquals(listOf("s1"), dao.getHiddenSongs().map { it.id })
        assertTrue(dao.search("a").isEmpty())
        assertTrue(dao.getById("s1")!!.isHidden)
    }

    @Test
    fun `registerPlay incrementa el contador y actualiza la ultima reproduccion`() = runTest {
        val dao = db.libraryDao()

        dao.upsert(song("s1", "A"))

        dao.registerPlay("s1", 1_000L)
        dao.registerPlay("s1", 2_000L)

        val loaded = dao.getById("s1")!!
        assertEquals(2L, loaded.playCount)
        assertEquals(2_000L, loaded.lastPlayedAtEpochMs)
    }

    @Test
    fun `setHidden false restaura la cancion a la biblioteca`() = runTest {
        val dao = db.libraryDao()

        dao.upsertAll(listOf(song("s1", "A"), song("s2", "B")))
        dao.setHidden("s1", true)
        dao.setHidden("s1", false)

        assertEquals(listOf("s1", "s2"), dao.getSongs().map { it.id })
        assertTrue(dao.observeHiddenSongs().first().isEmpty())
    }
}
