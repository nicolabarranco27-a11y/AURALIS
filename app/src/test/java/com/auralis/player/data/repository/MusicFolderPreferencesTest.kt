// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.data.repository

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MusicFolderPreferencesTest {

    private val treeMusic =
        "content://com.android.externalstorage.documents/tree/primary%3AMusic"

    @Test
    fun `por defecto no hay carpetas seleccionadas`() {
        val prefs = MusicFolderPreferences(ApplicationProvider.getApplicationContext())

        assertTrue(prefs.selectedFolders().isEmpty())
    }

    @Test
    fun `guarda carpetas y actualiza el flujo`() {
        val prefs = MusicFolderPreferences(ApplicationProvider.getApplicationContext())

        prefs.setSelectedFolders(setOf(treeMusic))

        assertEquals(setOf(treeMusic), prefs.selectedFolders())
        assertEquals(setOf(treeMusic), prefs.selectedFolders.value)
    }

    @Test
    fun `la seleccion persiste entre instancias`() {
        MusicFolderPreferences(ApplicationProvider.getApplicationContext())
            .setSelectedFolders(setOf(treeMusic))

        val recreated = MusicFolderPreferences(ApplicationProvider.getApplicationContext())

        assertEquals(setOf(treeMusic), recreated.selectedFolders())
    }
}
