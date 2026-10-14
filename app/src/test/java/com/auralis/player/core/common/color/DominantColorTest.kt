package com.auralis.player.core.common.color

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DominantColorTest {

    @Test
    fun `buffer vacio devuelve null`() {
        assertNull(dominantColorFromPixels(IntArray(0)))
    }

    @Test
    fun `un unico color saturado devuelve ese color`() {
        val pixels = IntArray(16) { 0xFF3355AA.toInt() }

        val result = dominantColorFromPixels(pixels)

        assertNotNull(result)
        assertEquals(0xFF3355AA.toInt(), result!!.toArgb())
    }

    @Test
    fun `los pixeles transparentes se ignoran`() {
        val pixels = IntArray(16) { 0x00FF0000 }

        assertNull(dominantColorFromPixels(pixels))
    }

    @Test
    fun `los grises devuelven null`() {
        val pixels = IntArray(16) { 0xFF808080.toInt() }

        assertNull(dominantColorFromPixels(pixels))
    }

    @Test
    fun `los colores muy oscuros se descartan`() {
        val pixels = IntArray(16) { 0xFF0A0A0A.toInt() }

        assertNull(dominantColorFromPixels(pixels))
    }

    @Test
    fun `gana el color mas frecuente ponderado por saturacion`() {
        val pixels = IntArray(64) { 0xFF3050A0.toInt() }
        val result = dominantColorFromPixels(pixels)

        assertNotNull(result)
        assertEquals(0xFF3050A0.toInt(), result!!.toArgb())
    }

    @Test
    fun `la saturacion se calcula correctamente`() {
        val red = RgbColor(255, 0, 0)
        val gray = RgbColor(120, 120, 120)
        val dark = RgbColor(0, 0, 0)

        assertTrue(red.saturation > gray.saturation)
        assertEquals(0f, dark.saturation, 0.0001f)
    }
}
