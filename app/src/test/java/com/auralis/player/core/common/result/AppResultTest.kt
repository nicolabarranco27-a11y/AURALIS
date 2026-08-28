package com.auralis.player.core.common.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppResultTest {

    @Test
    fun `of devuelve Success cuando el bloque no lanza`() {
        val result = AppResult.of { 42 }

        assertEquals(42, result.getOrNull())
    }

    @Test
    fun `of devuelve Failure cuando el bloque lanza`() {
        val result = AppResult.of<Int> { error("boom") }

        assertTrue(result is AppResult.Failure)
        assertNull(result.getOrNull())
    }

    @Test
    fun `getOrNull de Failure es nulo`() {
        val result: AppResult<Int> = AppResult.Failure(AppError.NotFound("no existe"))

        assertNull(result.getOrNull())
    }
}
