// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.core.common.format

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatTest {

    @Test
    fun `formatTotalDuration muestra minutos por debajo de una hora`() {
        assertEquals("0 min", formatTotalDuration(0))
        assertEquals("0 min", formatTotalDuration(59_000))
        assertEquals("1 min", formatTotalDuration(60_000))
        assertEquals("59 min", formatTotalDuration(59 * 60_000L))
    }

    @Test
    fun `formatTotalDuration muestra horas y minutos`() {
        assertEquals("1 h", formatTotalDuration(60 * 60_000L))
        assertEquals("1 h 5 min", formatTotalDuration(65 * 60_000L))
        assertEquals("2 h", formatTotalDuration(120 * 60_000L))
    }

    @Test
    fun `formatSongCount singular y plural`() {
        assertEquals("0 canciones", formatSongCount(0))
        assertEquals("1 canción", formatSongCount(1))
        assertEquals("2 canciones", formatSongCount(2))
    }
}