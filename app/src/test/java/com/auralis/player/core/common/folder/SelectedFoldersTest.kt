package com.auralis.player.core.common.folder

import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectedFoldersTest {

    private fun song(id: String, folderUri: String?, cover: String? = null) = Song(
        id = SongId(id),
        sourceUri = "content://media/audio/$id",
        title = id,
        sourceFolderUri = folderUri,
        coverReference = cover,
    )

    @Test
    fun `songsInSelectedFolder filtra por carpeta`() {
        val songs = listOf(
            song("a", "folder/1"),
            song("b", "folder/2"),
            song("c", "folder/1"),
        )

        assertEquals(
            listOf("a", "c"),
            songsInSelectedFolder(songs, "folder/1").map { it.id.value },
        )
    }

    @Test
    fun `songsInSelectedFolder con null devuelve toda la biblioteca`() {
        val songs = listOf(song("a", "folder/1"), song("b", null))

        assertEquals(listOf("a", "b"), songsInSelectedFolder(songs, null).map { it.id.value })
    }

    @Test
    fun `libraryFolders resume conteo y portada por carpeta`() {
        val songs = listOf(
            song("a", "folder/1", cover = "cover/a"),
            song("b", "folder/1"),
            song("c", "folder/2"),
            song("d", null),
        )

        val folders = libraryFolders(songs, listOf("folder/1", "folder/2"))

        assertEquals(listOf("folder/1", "folder/2"), folders.map { it.uri })
        assertEquals(2, folders[0].songCount)
        assertEquals("cover/a", folders[0].coverReference)
        assertEquals(1, folders[1].songCount)
    }

    @Test
    fun `libraryFolders incluye carpetas vacias seleccionadas`() {
        val folders = libraryFolders(emptyList(), listOf("folder/vacia"))

        assertEquals(1, folders.size)
        assertEquals(0, folders[0].songCount)
        assertTrue(folders[0].coverReference == null)
    }
}
