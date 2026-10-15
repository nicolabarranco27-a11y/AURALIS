package com.auralis.player.core.common.library

import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class MostPlayedSongsTest {

    private fun song(
        id: String,
        playCount: Long = 0L,
        lastPlayedAt: Instant? = null,
        isFavorite: Boolean = false,
    ) = Song(
        id = SongId(id),
        sourceUri = "content://media/audio/$id",
        title = id,
        playCount = playCount,
        lastPlayedAt = lastPlayedAt,
        isFavorite = isFavorite,
    )

    @Test
    fun `ordena por reproducciones y limita el resultado`() {
        val songs = listOf(
            song("a", playCount = 1),
            song("b", playCount = 5),
            song("c", playCount = 3),
        )

        val result = mostPlayedSongs(songs, limit = 2)

        assertEquals(listOf("b", "c"), result.map { it.id.value })
    }

    @Test
    fun `desempata por ultima reproduccion mas reciente`() {
        val older = song("old", playCount = 2, lastPlayedAt = Instant.ofEpochMilli(100))
        val newer = song("new", playCount = 2, lastPlayedAt = Instant.ofEpochMilli(500))

        val result = mostPlayedSongs(listOf(older, newer))

        assertEquals(listOf("new", "old"), result.map { it.id.value })
    }

    @Test
    fun `sin reproducciones cae a las favoritas`() {
        val songs = listOf(
            song("a"),
            song("b", isFavorite = true),
            song("c", isFavorite = true),
        )

        val result = mostPlayedSongs(songs)

        assertEquals(listOf("b", "c"), result.map { it.id.value })
    }

    @Test
    fun `sin favoritas ni reproducciones devuelve las primeras`() {
        val songs = listOf(song("a"), song("b"), song("c"))

        val result = mostPlayedSongs(songs, limit = 2)

        assertEquals(listOf("a", "b"), result.map { it.id.value })
    }

    @Test
    fun `limit no positivo devuelve vacio`() {
        val songs = listOf(song("a", playCount = 3))

        assertEquals(emptyList<Song>(), mostPlayedSongs(songs, limit = 0))
    }
}
