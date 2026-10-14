package com.auralis.player.feature.hidden

import androidx.test.core.app.ApplicationProvider
import com.auralis.player.domain.model.Album
import com.auralis.player.domain.model.Artist
import com.auralis.player.domain.model.Genre
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.QueueItem
import com.auralis.player.domain.model.RepeatMode
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import com.auralis.player.domain.repository.LibraryRepository
import com.auralis.player.domain.repository.PlaybackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
@OptIn(ExperimentalCoroutinesApi::class)
class HiddenSongsViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        ApplicationProvider.getApplicationContext<android.content.Context>()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun song(id: String, title: String) = Song(
        id = SongId(id),
        sourceUri = "content://media/audio/$id",
        title = title,
        artist = "Artista",
        album = "Album",
        durationMs = 100L,
        isHidden = true,
    )

    @Test
    fun `observeHiddenSongs alimenta el estado`() = runTest {
        val repo = FakeLibraryRepository(listOf(song("h1", "Oculta 1"), song("h2", "Oculta 2")))

        val viewModel = HiddenSongsViewModel(repo, FakePlaybackRepository())

        val state = viewModel.uiState.first()
        assertEquals(2, state.songs.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `restoreSong marca la cancion como visible`() = runTest {
        val repo = FakeLibraryRepository(listOf(song("h1", "Oculta 1")))

        val viewModel = HiddenSongsViewModel(repo, FakePlaybackRepository())
        viewModel.restoreSong(song("h1", "Oculta 1"))

        assertEquals(listOf("h1" to false), repo.hiddenCalls)
    }

    @Test
    fun `restoreAll restaura todas las canciones ocultas`() = runTest {
        val repo = FakeLibraryRepository(listOf(song("h1", "A"), song("h2", "B")))

        val viewModel = HiddenSongsViewModel(repo, FakePlaybackRepository())
        viewModel.restoreAll()

        assertEquals(2, repo.hiddenCalls.size)
        assertTrue(repo.hiddenCalls.all { !it.second })
    }

    private class FakeLibraryRepository(initialHidden: List<Song>) : LibraryRepository {
        private val hidden = MutableStateFlow(initialHidden)
        val hiddenCalls = mutableListOf<Pair<String, Boolean>>()

        override fun observeHiddenSongs(): Flow<List<Song>> = hidden.asStateFlow()
        override suspend fun setSongHidden(songId: SongId, hidden: Boolean) {
            hiddenCalls += songId.value to hidden
        }

        override suspend fun getSongs(): List<Song> = emptyList()
        override suspend fun getAlbums(): List<Album> = emptyList()
        override suspend fun getArtists(): List<Artist> = emptyList()
        override suspend fun getGenres(): List<Genre> = emptyList()
        override fun observeSongs(): Flow<List<Song>> = emptyFlow()
        override fun observeAlbums(): Flow<List<Album>> = emptyFlow()
        override fun observeArtists(): Flow<List<Artist>> = emptyFlow()
        override fun observeGenres(): Flow<List<Genre>> = emptyFlow()
        override fun observeSongsByAlbum(albumTitle: String, artist: String?): Flow<List<Song>> = emptyFlow()
        override fun observeSongsByArtist(artistName: String): Flow<List<Song>> = emptyFlow()
        override fun observeAlbumsByArtist(artistName: String): Flow<List<Album>> = emptyFlow()
        override fun observeLibraryChanges(): Flow<Unit> = emptyFlow()
        override suspend fun searchSongs(query: String): List<Song> = emptyList()
    }

    private class FakePlaybackRepository : PlaybackRepository {
        private val state = MutableStateFlow(PlaybackState.IDLE)
        override fun observePlaybackState(): Flow<PlaybackState> = state.asStateFlow()
        override fun observeQueue(): Flow<List<Song>> = emptyFlow()
        override suspend fun getCurrentState(): PlaybackState = state.value
        override fun play() {}
        override fun pause() {}
        override fun seekTo(positionMs: Long) {}
        override fun skipNext() {}
        override fun skipPrevious() {}
        override fun setShuffleEnabled(enabled: Boolean) {}
        override fun setRepeatMode(mode: RepeatMode) {}
        override fun setQueue(items: List<QueueItem>, startIndex: Int) {}
        override fun addSongToQueue(song: Song) {}
        override fun moveQueueItem(fromIndex: Int, toIndex: Int) {}
        override fun removeQueueItem(index: Int) {}
        override fun applyEqualizerBands(bands: List<Float>, preset: String) {}
    }
}