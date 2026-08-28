package com.auralis.player.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "genres", indices = [Index("name")])
data class GenreEntity(
    @PrimaryKey val id: String,
    val name: String,
    val songCount: Int,
)
