package com.auralis.player.core.common.format

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FolderLabelTest {

    @Test
    fun `muestra la ruta relativa sin el volumen`() {
        val uri =
            "content://com.android.externalstorage.documents/tree/primary%3AMusic%2FAlbums"

        assertEquals("Music/Albums", folderDisplayName(uri))
    }

    @Test
    fun `la raiz del volumen muestra almacenamiento interno`() {
        val uri = "content://com.android.externalstorage.documents/tree/primary%3A"

        assertEquals("Almacenamiento interno", folderDisplayName(uri))
    }

    @Test
    fun `decodifica espacios en el nombre de la carpeta`() {
        val uri =
            "content://com.android.externalstorage.documents/tree/primary%3AMi%20Musica"

        assertEquals("Mi Musica", folderDisplayName(uri))
    }
}
