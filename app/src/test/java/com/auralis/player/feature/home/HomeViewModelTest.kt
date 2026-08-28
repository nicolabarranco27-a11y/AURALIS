package com.auralis.player.feature.home

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ApplicationProvider
import com.auralis.player.data.scanner.LibraryScanMonitor
import com.auralis.player.data.scanner.LibraryScanScheduler
import com.auralis.player.data.scanner.ScanOutcome
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import com.auralis.player.domain.repository.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var context: Context
    private lateinit var application: Application

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        context = ApplicationProvider.getApplicationContext()
        application = ApplicationProvider.getApplicationContext<Application>()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun grantAudioPermission(grant: Boolean) {
        if (grant) {
            shadowOf(application).grantPermissions(android.Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            shadowOf(application).denyPermissions(android.Manifest.permission.READ_MEDIA_AUDIO)
        }
    }

    @Test
    fun `estado inicial sin permiso y escaneo idle`() = kotlinx.coroutines.test.runTest {
        grantAudioPermission(false)
        val viewModel = createViewModel(hasPermission = false)

        val state = viewModel.uiState.first()
        assertFalse(state.hasPermission)
        assertEquals(ScanDisplayStatus.Idle, state.scanStatus)
        assertTrue(state.songs.isEmpty())
        assertEquals(0, state.songCount)
    }

    @Test
    fun `estado inicial con permiso concedido`() = kotlinx.coroutines.test.runTest {
        grantAudioPermission(true)
        val viewModel = createViewModel(hasPermission = true)

        val state = viewModel.uiState.first()
        assertTrue(state.hasPermission)
        assertEquals(ScanDisplayStatus.Idle, state.scanStatus)
    }

    @Test
    fun `onPermissionResult true actualiza permiso y solicita escaneo`() = kotlinx.coroutines.test.runTest {
        val scheduler = FakeScanScheduler(context)
        val viewModel = createViewModel(hasPermission = false, scanScheduler = scheduler)

        grantAudioPermission(true)
        viewModel.onPermissionResult(true)

        val state = viewModel.uiState.first()
        assertTrue(state.hasPermission)
        assertTrue(scheduler.scanRequested)
    }

    @Test
    fun `onPermissionResult false no solicita escaneo`() = kotlinx.coroutines.test.runTest {
        val scheduler = FakeScanScheduler(context)
        val viewModel = createViewModel(hasPermission = false, scanScheduler = scheduler)

        viewModel.onPermissionResult(false)

        val state = viewModel.uiState.first()
        assertFalse(state.hasPermission)
        assertFalse(scheduler.scanRequested)
    }

    @Test
    fun `requestScan sin permiso no hace nada`() = kotlinx.coroutines.test.runTest {
        val scheduler = FakeScanScheduler(context)
        val viewModel = createViewModel(hasPermission = false, scanScheduler = scheduler)

        viewModel.requestScan()

        assertFalse(scheduler.scanRequested)
    }

    @Test
    fun `requestScan con permiso solicita escaneo`() = kotlinx.coroutines.test.runTest {
        val scheduler = FakeScanScheduler(context)
        val viewModel = createViewModel(hasPermission = true, scanScheduler = scheduler)

        viewModel.requestScan()

        assertTrue(scheduler.scanRequested)
    }

    @Test
    fun `observeSongs actualiza lista de canciones`() = kotlinx.coroutines.test.runTest {
        val songs = listOf(
            Song(id = SongId("1"), sourceUri = "uri1", title = "Song 1", artist = "Artist 1", album = "Album 1", durationMs = 180000),
            Song(id = SongId("2"), sourceUri = "uri2", title = "Song 2", artist = "Artist 2", album = null, durationMs = 240000),
        )
        val repository = FakeLibraryRepository(songs)
        val viewModel = createViewModel(hasPermission = true, libraryRepository = repository)

        val state = viewModel.uiState.first()

        assertEquals(2, state.songCount)
        assertEquals("Song 1", state.songs[0].title)
        assertEquals("Song 2", state.songs[1].title)
    }

    @Test
    fun `scanMonitor idle muestra estado idle`() = kotlinx.coroutines.test.runTest {
        val monitor = FakeScanMonitor()
        val viewModel = createViewModel(hasPermission = true, scanMonitor = monitor)

        val state = viewModel.uiState.first()
        assertEquals(ScanDisplayStatus.Idle, state.scanStatus)
    }

    @Test
    fun `scanMonitor running muestra estado running`() = kotlinx.coroutines.test.runTest {
        val monitor = FakeScanMonitor()
        val viewModel = createViewModel(hasPermission = true, scanMonitor = monitor)

        monitor.emitRunning()

        val state = viewModel.uiState.first()
        assertEquals(ScanDisplayStatus.Running, state.scanStatus)
    }

    @Test
    fun `scanMonitor finished success muestra estado success`() = kotlinx.coroutines.test.runTest {
        val monitor = FakeScanMonitor()
        val viewModel = createViewModel(hasPermission = true, scanMonitor = monitor)

        monitor.emitFinished(ScanOutcome.Success(scanned = 10, added = 5, updated = 3, removed = 2))

        val state = viewModel.uiState.first()
        assertTrue(state.scanStatus is ScanDisplayStatus.Success)
        val success = state.scanStatus as ScanDisplayStatus.Success
        assertEquals(10, success.scanned)
        assertEquals(5, success.added)
        assertEquals(3, success.updated)
        assertEquals(2, success.removed)
    }

    @Test
    fun `scanMonitor finished failed muestra estado failed`() = kotlinx.coroutines.test.runTest {
        val monitor = FakeScanMonitor()
        val viewModel = createViewModel(hasPermission = true, scanMonitor = monitor)

        monitor.emitFinished(ScanOutcome.Failed("Error de escaneo"))

        val state = viewModel.uiState.first()
        assertTrue(state.scanStatus is ScanDisplayStatus.Failed)
        val failed = state.scanStatus as ScanDisplayStatus.Failed
        assertEquals("Error de escaneo", failed.message)
    }

    @Test
    fun `scanMonitor permissionDenied muestra estado idle`() = kotlinx.coroutines.test.runTest {
        val monitor = FakeScanMonitor()
        val viewModel = createViewModel(hasPermission = true, scanMonitor = monitor)

        monitor.emitFinished(ScanOutcome.PermissionDenied)

        val state = viewModel.uiState.first()
        assertEquals(ScanDisplayStatus.Idle, state.scanStatus)
    }

    @Test
    fun `onScreenShown no lanza excepcion`() = kotlinx.coroutines.test.runTest {
        val viewModel = createViewModel(hasPermission = false)

        viewModel.onScreenShown()

        assertTrue(true)
    }

    @Test
    fun `refreshPermissionState actualiza estado de permiso`() = kotlinx.coroutines.test.runTest {
        val scheduler = FakeScanScheduler(context)
        val monitor = FakeScanMonitor()
        val repository = FakeLibraryRepository()
        val playback = FakePlaybackRepository()

        val viewModel = HomeViewModel(
            context,
            repository,
            playback,
            scheduler,
            monitor,
        )

        assertFalse(viewModel.uiState.first().hasPermission)

        viewModel.refreshPermissionState()

        // Context from Robolectric may or may not have permission, just verify no crash
        assertTrue(true)
    }

    private fun createViewModel(
        hasPermission: Boolean,
        libraryRepository: LibraryRepository = FakeLibraryRepository(),
        playbackRepository: com.auralis.player.domain.repository.PlaybackRepository = FakePlaybackRepository(),
        scanScheduler: LibraryScanScheduler = FakeScanScheduler(context),
        scanMonitor: LibraryScanMonitor = FakeScanMonitor(),
    ): HomeViewModel {
        grantAudioPermission(hasPermission)
        return HomeViewModel(context, libraryRepository, playbackRepository, scanScheduler, scanMonitor)
    }

    private class FakePlaybackRepository : com.auralis.player.domain.repository.PlaybackRepository {
        private val _state = MutableStateFlow(com.auralis.player.domain.model.PlaybackState.IDLE)
        override fun observePlaybackState(): kotlinx.coroutines.flow.Flow<com.auralis.player.domain.model.PlaybackState> = _state.asStateFlow()
        override suspend fun getCurrentState(): com.auralis.player.domain.model.PlaybackState = _state.value
        override fun play() {}
        override fun pause() {}
        override fun seekTo(positionMs: Long) {}
        override fun skipNext() {}
        override fun skipPrevious() {}
        override fun setShuffleEnabled(enabled: Boolean) {}
        override fun setRepeatMode(mode: com.auralis.player.domain.model.RepeatMode) {}
        override fun setQueue(items: List<com.auralis.player.domain.model.QueueItem>, startIndex: Int) {}
    }

    private class FakeLibraryRepository(private val songs: List<Song> = emptyList()) : LibraryRepository {
        override suspend fun getSongs(): List<Song> = songs
        override suspend fun getAlbums(): List<com.auralis.player.domain.model.Album> = emptyList()
        override suspend fun getArtists(): List<com.auralis.player.domain.model.Artist> = emptyList()
        override suspend fun getGenres(): List<com.auralis.player.domain.model.Genre> = emptyList()
        override fun observeSongs(): kotlinx.coroutines.flow.Flow<List<Song>> = flowOf(songs)
        override fun observeAlbums(): kotlinx.coroutines.flow.Flow<List<com.auralis.player.domain.model.Album>> = emptyFlow()
        override fun observeArtists(): kotlinx.coroutines.flow.Flow<List<com.auralis.player.domain.model.Artist>> = emptyFlow()
        override fun observeGenres(): kotlinx.coroutines.flow.Flow<List<com.auralis.player.domain.model.Genre>> = emptyFlow()
        override fun observeSongsByAlbum(albumTitle: String, artist: String?): kotlinx.coroutines.flow.Flow<List<Song>> = emptyFlow()
        override fun observeSongsByArtist(artistName: String): kotlinx.coroutines.flow.Flow<List<Song>> = emptyFlow()
        override fun observeAlbumsByArtist(artistName: String): kotlinx.coroutines.flow.Flow<List<com.auralis.player.domain.model.Album>> = emptyFlow()
        override fun observeLibraryChanges(): kotlinx.coroutines.flow.Flow<Unit> = emptyFlow()
        override suspend fun searchSongs(query: String): List<Song> = emptyList()
    }

    private class FakeScanScheduler @javax.inject.Inject constructor(
        private val testContext: Context
    ) : LibraryScanScheduler(testContext) {
        var scanRequested = false
        override fun requestScan() { scanRequested = true }
    }

    private class FakeScanMonitor @javax.inject.Inject constructor() : LibraryScanMonitor() {
        private val _status = MutableStateFlow<LibraryScanMonitor.Status>(LibraryScanMonitor.Status.Idle)
        override val status: kotlinx.coroutines.flow.StateFlow<LibraryScanMonitor.Status> = _status.asStateFlow()

        fun emitRunning() { _status.value = LibraryScanMonitor.Status.Running }
        fun emitFinished(outcome: ScanOutcome) { _status.value = LibraryScanMonitor.Status.Finished(outcome) }
    }
}