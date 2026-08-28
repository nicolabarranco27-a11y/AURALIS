package com.auralis.player.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "playlists", indices = [Index("name")])
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val isSystemPlaylist: Boolean,
)
