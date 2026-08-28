package com.auralis.player.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Relacion ordenada playlist-cancion.
 * PK compuesta (playlistId, songId); el prefijo izquierdo ya indexa
 * las consultas por playlist, no se necesita indice adicional.
 */
@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PlaylistSongEntity(
    val playlistId: String,
    val songId: String,
    val position: Int,
)
