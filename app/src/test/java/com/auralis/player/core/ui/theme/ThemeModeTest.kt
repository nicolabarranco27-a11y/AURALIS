package com.auralis.player.core.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `fromKey reconoce los modos guardados`() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromKey("LIGHT"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromKey("DARK"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromKey("SYSTEM"))
    }

    @Test
    fun `fromKey es insensible a mayusculas`() {
        assertEquals(ThemeMode.DARK, ThemeMode.fromKey("dark"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromKey("Light"))
    }

    @Test
    fun `fromKey cae en SYSTEM ante valores invalidos o nulos`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromKey(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromKey("desconocido"))
    }
}
