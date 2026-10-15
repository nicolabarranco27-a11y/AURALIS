package com.auralis.player.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["mediaStoreId"], unique = true),
        Index("uri"),
        Index("title"),
        Index("artist"),
        Index("album"),
        Index("albumArtist"),
        Index("genre"),
        Index("dateAddedEpochMs"),
        Index("sourceFolderUri"),
    ],
)
data class SongEntity(
    @PrimaryKey val id: String,
    val mediaStoreId: Long,
    val uri: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val albumArtist: String?,
    val genre: String?,
    val year: Int?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val durationMs: Long?,
    val dateAddedEpochMs: Long?,
    val dateModifiedEpochMs: Long?,
    val mimeType: String?,
    val sizeBytes: Long?,
    val path: String?,
    val playCount: Long,
    val lastPlayedAtEpochMs: Long?,
    val isFavorite: Boolean,
    /**
     * true = el usuario oculto la cancion de la biblioteca (accion reversible).
     * Se conserva ante re-escaneos y no elimina el archivo del dispositivo.
     */
    val isHidden: Boolean = false,
    /** URI de la portada (referencia local o de MediaStore). */
    val coverUri: String? = null,
    /**
     * URI de arbol ("tree URI") de la carpeta elegida por el usuario (SAF) que
     * contiene esta cancion. Es la identidad con la que Inicio agrupa la
     * biblioteca por carpetas. null en filas escaneadas antes de la migracion
     * 5->6 (se rellena al re-escanear) o si el audio no pertenece a ninguna.
     */
    val sourceFolderUri: String? = null,
    /**
     * Clave de busqueda premormalizada (minusculas y sin acentos) de
     * title+artist+album. Evita recalcular REPLACE/LOWER por fila en cada
     * consulta, critico en bibliotecas grandes.
     */
    val searchKey: String = "",
    /**
     * false = la cancion ya no existe en MediaStore (eliminacion logica).
     * Preserva playlists, favoritos e historico ante desapariciones
     * temporales del archivo.
     */
    val isAvailable: Boolean = true,
)
