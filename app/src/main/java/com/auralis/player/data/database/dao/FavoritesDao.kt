package com.auralis.player.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Query("SELECT id FROM songs WHERE isFavorite = 1 AND isAvailable = 1 ORDER BY title COLLATE NOCASE")
    fun observeFavoriteIds(): Flow<List<String>>

    @Query("SELECT isFavorite FROM songs WHERE id = :id")
    suspend fun isFavorite(id: String): Boolean?

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE songs SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggle(id: String)
}
