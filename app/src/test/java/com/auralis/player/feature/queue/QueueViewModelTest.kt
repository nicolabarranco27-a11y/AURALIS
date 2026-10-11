package com.auralis.player.feature.queue

import com.auralis.player.domain.model.PlaybackState
import com.auralis.player.domain.model.QueueItem
import com.auralis.player.domain.model.RepeatMode
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import com.auralis.player.domain.repository.PlaybackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QueueViewModelTest {

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
    fun `expone la cola y el indice de la cancion actual`() = runTest(dispatcher) {
        val repository = FakePlaybackRepository()
        repository.queue.value = listOf(song("a"), song("b"), song("c"))
        repository.state.value = PlaybackState.IDLE.copy(currentSong = song("b"))
        val viewModel = QueueViewModel(repository)

        val job = launch { viewModel.uiState.collect {} }
        runCurrent()

        assertEquals(listOf("a", "b", "c"), viewModel.uiState.value.queue.map { it.id.value })
        assertEquals(1, viewModel.uiState.value.currentIndex)
        job.cancel()
    }

    @Test
    fun `onSongClick reemplaza la cola posicionando el indice`() = runTest(dispatcher) {
        val repository = FakePlaybackRepository()
        repository.queue.value = listOf(song("a"), song("b"), song("c"))
        val viewModel = QueueViewModel(repository)

        val job = launch { viewModel.uiState.collect {} }
        runCurrent()

        viewModel.onSongClick(2)

        assertEquals(2, repository.lastSetQueueIndex)
        assertEquals(listOf("a", "b", "c"), repository.lastSetQueueSongs?.map { it.id.value })
        job.cancel()
    }

    @Test
    fun `removeQueueItem delega en el repositorio`() = runTest(dispatcher) {
        val repository = FakePlaybackRepository()
        val viewModel = QueueViewModel(repository)

        viewModel.removeQueueItem(1)

        assertEquals(1, repository.lastRemovedIndex)
    }

    private class FakePlaybackRepository : PlaybackRepository {
        val state = MutableStateFlow(PlaybackState.IDLE)
        val queue = MutableStateFlow<List<Song>>(emptyList())
        var lastSetQueueIndex: Int? = null
        var lastSetQueueSongs: List<Song>? = null
        var lastRemovedIndex: Int? = null

        override fun observePlaybackState(): Flow<PlaybackState> = state.asStateFlow()
        override fun observeQueue(): Flow<List<Song>> = queue.asStateFlow()
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
        override fun removeQueueItem(index: Int) { lastRemovedIndex = index }
    }
}
