package com.auralis.player.feature.favorites

import com.auralis.player.domain.model.Album
import com.auralis.player.domain.model.Artist
import com.auralis.player.domain.model.Genre
import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.QueueItem
import com.auralis.player.domain.model.RepeatMode
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import com.auralis.player.domain.repository.FavoritesRepository
import com.auralis.player.domain.repository.LibraryRepository
import com.auralis.player.domain.repository.PlaybackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun song(id: String) = Song(id = SongId(id), sourceUri = "uri://$id", title = id)

    @Test
    fun `filtra las canciones marcadas como favoritas`() = runTest(dispatcher) {
        val library = FakeLibraryRepository(listOf(song("a"), song("b"), song("c")))
        val favorites = FakeFavoritesRepository(setOf(SongId("a"), SongId("c")))

        val viewModel = FavoritesViewModel(library, FakePlaybackRepository(), favorites)
        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf("a", "c"), viewModel.uiState.value.songs.map { it.id.value })
    }

    @Test
    fun `onSongClick reproduce solo las favoritas`() = runTest(dispatcher) {
        val library = FakeLibraryRepository(listOf(song("a"), song("b")))
        val favorites = FakeFavoritesRepository(setOf(SongId("b")))
        val playback = FakePlaybackRepository()

        val viewModel = FavoritesViewModel(library, playback, favorites)
        runCurrent()

        viewModel.onSongClick(song("b"))

        assertEquals(listOf("b"), playback.lastSetQueueSongs?.map { it.id.value })
        assertEquals(0, playback.lastSetQueueIndex)
    }

    @Test
    fun `toggleFavorite delega en el repositorio`() = runTest(dispatcher) {
        val favorites = FakeFavoritesRepository(emptySet())
        val viewModel = FavoritesViewModel(FakeLibraryRepository(emptyList()), FakePlaybackRepository(), favorites)

        viewModel.toggleFavorite(song("a"))
        runCurrent()

        assertEquals(SongId("a"), favorites.lastToggled)
    }

    private class FakeLibraryRepository(private val songs: List<Song>) : LibraryRepository {
        override suspend fun getSongs(): List<Song> = songs
        override suspend fun getAlbums(): List<Album> = emptyList()
        override suspend fun getArtists(): List<Artist> = emptyList()
        override suspend fun getGenres(): List<Genre> = emptyList()
        override fun observeSongs(): Flow<List<Song>> = flowOf(songs)
        override fun observeAlbums(): Flow<List<Album>> = emptyFlow()
        override fun observeArtists(): Flow<List<Artist>> = emptyFlow()
        override fun observeGenres(): Flow<List<Genre>> = emptyFlow()
        override fun observeSongsByAlbum(albumTitle: String, artist: String?): Flow<List<Song>> = emptyFlow()
        override fun observeSongsByArtist(artistName: String): Flow<List<Song>> = emptyFlow()
        override fun observeAlbumsByArtist(artistName: String): Flow<List<Album>> = emptyFlow()
        override fun observeLibraryChanges(): Flow<Unit> = emptyFlow()
        override suspend fun searchSongs(query: String): List<Song> = emptyList()
    }

    private class FakeFavoritesRepository(initial: Set<SongId>) : FavoritesRepository {
        private val favs = MutableStateFlow(initial)
        var lastToggled: SongId? = null
        override suspend fun isFavorite(songId: SongId): Boolean = songId in favs.value
        override fun observeFavorites(): Flow<Set<SongId>> = favs.asStateFlow()
        override suspend fun addFavorite(songId: SongId) { favs.value = favs.value + songId }
        override suspend fun removeFavorite(songId: SongId) { favs.value = favs.value - songId }
        override suspend fun toggleFavorite(songId: SongId): Boolean {
            lastToggled = songId
            val nowFav = songId !in favs.value
            favs.value = if (nowFav) favs.value + songId else favs.value - songId
            return nowFav
        }
    }

    private class FakePlaybackRepository : PlaybackRepository {
        val state = MutableStateFlow(PlaybackState.IDLE)
        var lastSetQueueIndex: Int? = null
        var lastSetQueueSongs: List<Song>? = null
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
        override fun setQueue(items: List<QueueItem>, startIndex: Int) {
            lastSetQueueSongs = items.map { it.song }
            lastSetQueueIndex = startIndex
        }

        override fun addSongToQueue(song: Song) {}
        override fun moveQueueItem(fromIndex: Int, toIndex: Int) {}
        override fun removeQueueItem(index: Int) {}
        override fun applyEqualizerBands(bands: List<Float>, preset: String) {}
    }
}
