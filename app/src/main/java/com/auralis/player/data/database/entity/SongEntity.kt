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
    /** URI de la portada (referencia local o de MediaStore). */
    val coverUri: String? = null,
    /**
     * false = la cancion ya no existe en MediaStore (eliminacion logica).
     * Preserva playlists, favoritos e historico ante desapariciones
     * temporales del archivo.
     */
    val isAvailable: Boolean = true,
)
