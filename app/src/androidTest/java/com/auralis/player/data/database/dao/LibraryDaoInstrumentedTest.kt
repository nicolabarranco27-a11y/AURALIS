// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.data.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.auralis.player.data.database.AppDatabase
import com.auralis.player.data.database.entity.SongEntity
import com.auralis.player.data.database.mapper.buildSearchKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifica el acceso a datos sobre SQLite real del dispositivo (no Robolectric),
 * cubriendo la ruta que corre en celulares de todas las gamas.
 */
@RunWith(AndroidJUnit4::class)
class LibraryDaoInstrumentedTest {

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
        searchKey = buildSearchKey(title, artist, null),
    )

    @Test
    fun insertaYconsulta_enDeviceReal() = runBlocking {
        val dao = db.libraryDao()

        dao.upsert(song("s1", "Nightcall", artist = "Kavinsky"))

        val stored = dao.getById("s1")
        assertEquals("Nightcall", stored?.title)
        assertEquals("Kavinsky", stored?.artist)
    }

    @Test
    fun busqueda_insensibleAcentosYMayusculas_enDeviceReal() = runBlocking {
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
        assertTrue(dao.search("inexistente").isEmpty())
    }

    @Test
    fun observeSongs_emiteOrdenadoPorTitulo_enDeviceReal() = runBlocking {
        val dao = db.libraryDao()

        dao.upsertAll(listOf(song("s2", "Beta"), song("s1", "alpha")))

        assertEquals(listOf("alpha", "Beta"), dao.observeSongs().first().map { it.title })
    }

    @Test
    fun markUnavailable_ocultaSinBorrar_enDeviceReal() = runBlocking {
        val dao = db.libraryDao()

        dao.upsertAll(listOf(song("s1", "A"), song("s2", "B")))

        val removed = dao.markUnavailable(listOf("s1"))

        assertEquals(1, removed)
        assertTrue(dao.getSongs().map { it.id } == listOf("s2"))
        assertEquals(2, dao.getAllIncludingUnavailable().size)
        assertNull(null)
    }
}
