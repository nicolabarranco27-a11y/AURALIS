package com.auralis.player.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Carpetas de musica que el usuario eligio escanear.
 *
 * Cada entrada es el URI de arbol ("tree URI") devuelto por el selector de
 * documentos del sistema (Storage Access Framework), por ejemplo
 * `content://com.android.externalstorage.documents/tree/primary%3AMusic`.
 * Persistido en SharedPreferences y expuesto como StateFlow para que la UI y
 * el escaneo reaccionen sin re-consultar.
 */
@Singleton
class MusicFolderPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _selectedFolders = MutableStateFlow(loadSelectedFolders())
    val selectedFolders: StateFlow<Set<String>> = _selectedFolders.asStateFlow()

    /** URIs de carpeta seleccionadas (sincrono, ya en memoria). */
    fun selectedFolders(): Set<String> = _selectedFolders.value

    fun setSelectedFolders(folders: Set<String>) {
        val snapshot = folders.toSet()
        prefs.edit()
            .putStringSet(KEY_SELECTED_FOLDERS, snapshot)
            .apply()
        _selectedFolders.value = snapshot
    }

    private fun loadSelectedFolders(): Set<String> =
        prefs.getStringSet(KEY_SELECTED_FOLDERS, null)?.toSet() ?: emptySet()

    private companion object {
        const val PREFS_NAME = "auralis_library_prefs"
        const val KEY_SELECTED_FOLDERS = "selected_folder_uris"
    }
}
