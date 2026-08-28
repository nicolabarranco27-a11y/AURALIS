package com.auralis.player.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.auralis.player.data.database.AppDatabase
import com.auralis.player.data.database.entity.SongEntity
import com.auralis.player.domain.model.SongId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomFavoritesRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: RoomFavoritesRepository

    private fun song(id: String, favorite: Boolean = false) = SongEntity(
        id = id,
        mediaStoreId = id.hashCode().toLong(),
        uri = "uri://$id",
        title = "T $id",
        artist = null,
        album = null,
        albumArtist = null,
        genre = null,
        year = null,
        trackNumber = null,
        discNumber = null,
        durationMs = 1L,
        dateAddedEpochMs = null,
        dateModifiedEpochMs = null,
        mimeType = null,
        sizeBytes = null,
        path = null,
        playCount = 0L,
        lastPlayedAtEpochMs = null,
        isFavorite = favorite,
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = RoomFavoritesRepository(db.favoritesDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `toggle alterna y devuelve el nuevo estado`() = runTest {
        db.libraryDao().upsert(song("s1"))

        assertTrue(repository.toggleFavorite(SongId("s1")))
        assertTrue(repository.isFavorite(SongId("s1")))

        assertFalse(repository.toggleFavorite(SongId("s1")))
        assertFalse(repository.isFavorite(SongId("s1")))
    }

    @Test
    fun `observeFavorites refleja los ids favoritos`() = runTest {
        db.libraryDao().upsertAll(listOf(song("a", favorite = true), song("b")))

        val favorites = repository.observeFavorites().first()

        assertEquals(setOf("a"), favorites.map { it.value }.toSet())
        assertFalse(favorites.contains(SongId("b")))
    }

    @Test
    fun `cancion inexistente devuelve false y toggle no crea estado`() = runTest {
        assertFalse(repository.isFavorite(SongId("fantasma")))
        assertFalse(repository.toggleFavorite(SongId("fantasma")))
    }

    @Test
    fun `add y remove favorito explicitos`() = runTest {
        db.libraryDao().upsert(song("x"))

        repository.addFavorite(SongId("x"))
        assertTrue(repository.isFavorite(SongId("x")))

        repository.removeFavorite(SongId("x"))
        assertFalse(repository.isFavorite(SongId("x")))
    }
}
