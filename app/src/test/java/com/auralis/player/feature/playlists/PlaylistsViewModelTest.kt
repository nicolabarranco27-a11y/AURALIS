// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.feature.playlists

import androidx.test.core.app.ApplicationProvider
import com.auralis.player.domain.model.Playlist
import com.auralis.player.domain.model.PlaylistId
import com.auralis.player.domain.model.PlaylistSong
import com.auralis.player.domain.model.PlaylistSummary
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import com.auralis.player.domain.repository.PlaylistRepository
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistsViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        ApplicationProvider.getApplicationContext<android.content.Context>()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observePlaylistSummaries alimenta el estado`() = runTest {
        val repo = FakePlaylistRepository(
            summaries = MutableStateFlow(
                listOf(
                    PlaylistSummary(PlaylistId("p1"), "Lista", songCount = 2, totalDurationMs = 1000),
                )
            )
        )

        val viewModel = PlaylistsViewModel(repo)
        val state = viewModel.uiState.first()

        assertEquals(1, state.playlists.size)
        assertEquals("Lista", state.playlists.first().name)
        assertFalse(state.isLoading)
    }

    @Test
    fun `createPlaylist delega nombre y descripcion`() = runTest {
        val repo = FakePlaylistRepository()
        val viewModel = PlaylistsViewModel(repo)

        viewModel.createPlaylist("Lista", "desc")

        assertEquals(listOf("Lista" to "desc"), repo.createCalls)
    }

    @Test
    fun `editPlaylist renombra y actualiza descripcion`() = runTest {
        val repo = FakePlaylistRepository()
        val viewModel = PlaylistsViewModel(repo)

        viewModel.editPlaylist(PlaylistId("p1"), "Nuevo", "nueva desc")

        assertEquals(listOf("p1" to "Nuevo"), repo.renameCalls)
        assertEquals(listOf("p1" to "nueva desc"), repo.descriptionCalls)
    }

    @Test
    fun `deletePlaylist delega el id`() = runTest {
        val repo = FakePlaylistRepository()
        val viewModel = PlaylistsViewModel(repo)

        viewModel.deletePlaylist(PlaylistId("p9"))

        assertEquals(listOf("p9"), repo.deleteCalls)
    }

    private class FakePlaylistRepository(
        summaries: MutableStateFlow<List<PlaylistSummary>> = MutableStateFlow(emptyList()),
    ) : PlaylistRepository {

        private val summariesFlow = summaries
        val createCalls = mutableListOf<Pair<String, String?>>()
        val renameCalls = mutableListOf<Pair<String, String>>()
        val descriptionCalls = mutableListOf<Pair<String, String?>>()
        val deleteCalls = mutableListOf<String>()

        override fun observePlaylists(): Flow<List<Playlist>> = emptyFlow()

        override fun observePlaylistSummaries(): Flow<List<PlaylistSummary>> = summariesFlow.asStateFlow()

        override suspend fun getPlaylist(id: PlaylistId): Playlist? = null

        override suspend fun getSongs(id: PlaylistId): List<PlaylistSong> = emptyList()

        override fun observeSongs(id: PlaylistId): Flow<List<Song>> = emptyFlow()

        override suspend fun createPlaylist(name: String, description: String?): PlaylistId {
            createCalls += name to description
            return PlaylistId("generated")
        }

        override suspend fun renamePlaylist(id: PlaylistId, name: String) {
            renameCalls += id.value to name
        }

        override suspend fun updateDescription(id: PlaylistId, description: String?) {
            descriptionCalls += id.value to description
        }

        override suspend fun deletePlaylist(id: PlaylistId) {
            deleteCalls += id.value
        }

        override suspend fun addSong(playlistId: PlaylistId, songId: SongId) {}

        override suspend fun removeSong(playlistId: PlaylistId, songId: SongId) {}

        override suspend fun moveSong(playlistId: PlaylistId, songId: SongId, newPosition: Int) {}
    }
}