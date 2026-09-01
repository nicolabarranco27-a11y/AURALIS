package com.auralis.player.data.scanner

import android.app.Application
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.auralis.player.data.database.AppDatabase
import com.auralis.player.data.database.entity.PlaylistEntity
import com.auralis.player.data.database.entity.PlaylistSongEntity
import com.auralis.player.data.mediastore.AudioDiscoverySource
import com.auralis.player.data.mediastore.MediaStoreAudio
import com.auralis.player.data.metadata.MetadataExtractor
import com.auralis.player.data.metadata.TrackMetadata
import kotlinx.coroutines.CancellationException
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LibraryScannerTest {

    private lateinit var db: AppDatabase
    private lateinit var monitor: LibraryScanMonitor
    private lateinit var scanner: DefaultLibraryScanner

    private var discovered: List<MediaStoreAudio> = emptyList()
    private var extractorBehavior: () -> TrackMetadata = { TrackMetadata() }
    private var extractorCalls = 0

    private val discoverySource = object : AudioDiscoverySource {
        override suspend fun getAudioFiles(): List<MediaStoreAudio> = discovered
    }

    private val metadataExtractor = object : MetadataExtractor {
        override suspend fun extract(uri: Uri): TrackMetadata {
            extractorCalls++
            return extractorBehavior()
        }
    }

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(app).grantPermissions(android.Manifest.permission.READ_MEDIA_AUDIO)
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        monitor = LibraryScanMonitor()
        scanner = DefaultLibraryScanner(
            context = ApplicationProvider.getApplicationContext(),
            discoverySource = discoverySource,
            libraryDao = db.libraryDao(),
            metadataExtractor = metadataExtractor,
            scanMonitor = monitor,
            ioDispatcher = kotlinx.coroutines.Dispatchers.Unconfined,
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun audio(
        id: Long,
        title: String,
        modified: Long? = 100L,
        size: Long? = 1000L,
    ) = MediaStoreAudio(
        mediaStoreId = id,
        albumId = id * 10,
        uri = "content://media/audio/$id",
        displayName = "$title.mp3",
        title = title,
        artist = null,
        album = null,
        albumArtist = null,
        year = null,
        trackNumber = null,
        discNumber = null,
        durationMs = 120_000L,
        dateAddedEpochMs = 50L,
        dateModifiedEpochMs = modified,
        mimeType = "audio/mpeg",
        sizeBytes = size,
        path = "/music/$title.mp3",
    )

    private suspend fun dao() = db.libraryDao()

    @Test
    fun `inserta canciones nuevas y enriquece con metadata`() = runTest {
        discovered = listOf(audio(1, "Alpha"), audio(2, "Beta"))
        extractorBehavior = { TrackMetadata(genre = "Synthwave", year = 1986) }

        val outcome = scanner.sync()

        assertTrue(outcome is ScanOutcome.Success)
        outcome as ScanOutcome.Success
        assertEquals(2, outcome.added)
        assertEquals(2, extractorCalls)

        val songs = dao().getSongs()
        assertEquals(listOf("1", "2"), songs.map { it.id })
        assertEquals("Synthwave", songs.first { it.id == "1" }.genre)
        assertEquals(1986, songs.first { it.id == "1" }.year)
    }

    @Test
    fun `segunda ejecucion sin cambios es idempotente`() = runTest {
        discovered = listOf(audio(1, "Alpha"))
        scanner.sync()
        extractorCalls = 0

        val second = scanner.sync() as ScanOutcome.Success

        assertEquals(0, second.added)
        assertEquals(0, second.updated)
        assertEquals(0, second.removed)
        assertEquals(0, extractorCalls)
    }

    @Test
    fun `cancion modificada se actualiza conservando playCount e favorito`() = runTest {
        discovered = listOf(audio(1, "Viejo"))
        scanner.sync()

        dao().upsert(dao().getById("1")!!.copy(playCount = 9L, isFavorite = true))
        extractorCalls = 0
        discovered = listOf(audio(1, "Nuevo", modified = 200L))

        val outcome = scanner.sync() as ScanOutcome.Success

        assertEquals(1, outcome.updated)
        assertEquals(1, extractorCalls)
        val updated = dao().getById("1")!!
        assertEquals("Nuevo", updated.title)
        assertEquals(200L, updated.dateModifiedEpochMs)
        assertEquals(9L, updated.playCount)
        assertTrue(updated.isFavorite)
    }

    @Test
    fun `cancion eliminada se marca no disponible y preserva playlist`() = runTest {
        discovered = listOf(audio(1, "Alpha"), audio(2, "Beta"))
        scanner.sync()

        db.playlistDao().insertPlaylist(
            PlaylistEntity("p1", name = "Lista", description = null, createdAtEpochMs = 0, updatedAtEpochMs = 0, isSystemPlaylist = false),
        )
        db.playlistDao().insertSong(PlaylistSongEntity("p1", "1", position = 0))

        discovered = listOf(audio(2, "Beta"))
        val outcome = scanner.sync() as ScanOutcome.Success

        assertEquals(1, outcome.removed)
        assertFalse(dao().getById("1")!!.isAvailable)
        assertFalse(dao().getSongs().any { it.id == "1" })
        assertEquals("1", db.playlistDao().getSongs("p1").first().songId)
        assertTrue(dao().getAllIncludingUnavailable().any { it.id == "1" })
    }

    @Test
    fun `cancion que reaparece se restaura`() = runTest {
        discovered = listOf(audio(1, "Alpha"))
        scanner.sync()
        discovered = emptyList()
        scanner.sync()
        discovered = listOf(audio(1, "Alpha"))
        val outcome = scanner.sync() as ScanOutcome.Success

        assertEquals(1, outcome.updated)
        assertEquals(listOf("1"), dao().getSongs().map { it.id })
    }

    @Test
    fun `fallo de metadata en una cancion no aborta el escaneo`() = runTest {
        discovered = listOf(audio(1, "Rota"), audio(2, "Sana"))
        extractorBehavior = {
            if (extractorCalls == 1) error("archivo corrupto") else TrackMetadata(album = "OK")
        }

        val outcome = scanner.sync()

        assertTrue(outcome is ScanOutcome.Success)
        assertEquals(2, (outcome as ScanOutcome.Success).added)
        assertNull(dao().getById("1")?.album)
        assertEquals("OK", dao().getById("2")?.album)
    }

    @Test
    fun `sin permiso devuelve PermissionDenied y no toca Room`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(app).denyPermissions(android.Manifest.permission.READ_MEDIA_AUDIO)
        discovered = listOf(audio(1, "Alpha"))

        val outcome = scanner.sync()

        assertTrue(outcome is ScanOutcome.PermissionDenied)
        assertTrue(dao().getSongs().isEmpty())
    }

    @Test
    fun `la cancelacion se propaga y no se convierte en resultado`() = runTest {
        discovered = listOf(audio(1, "Alpha"))
        extractorBehavior = { throw CancellationException("worker cancelado") }

        try {
            scanner.sync()
            throw AssertionError("se esperaba CancellationException")
        } catch (expected: CancellationException) {
            assertEquals(LibraryScanMonitor.Status.Idle, monitor.status.value)
        }
    }

    @Test
    fun `el monitor refleja el ciclo de vida del escaneo`() = runTest {
        discovered = listOf(audio(1, "Alpha"))

        scanner.sync()

        val status = monitor.status.value
        assertTrue(status is LibraryScanMonitor.Status.Finished)
        assertTrue((status as LibraryScanMonitor.Status.Finished).outcome is ScanOutcome.Success)
    }
}
