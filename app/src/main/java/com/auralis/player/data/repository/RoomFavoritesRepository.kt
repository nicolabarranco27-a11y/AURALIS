package com.auralis.player.data.repository

import com.auralis.player.data.database.dao.FavoritesDao
import com.auralis.player.domain.model.SongId
import com.auralis.player.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Si la cancion no existe en el catalogo, toggleFavorite no modifica nada
 * y devuelve false; no se inventa estado para canciones inexistentes.
 */
@Singleton
class RoomFavoritesRepository @Inject constructor(
    private val favoritesDao: FavoritesDao,
) : FavoritesRepository {

    override suspend fun isFavorite(songId: SongId): Boolean =
        favoritesDao.isFavorite(songId.value) ?: false

    override fun observeFavorites(): Flow<Set<SongId>> =
        favoritesDao.observeFavoriteIds().map { ids -> ids.map(::SongId).toSet() }

    override suspend fun addFavorite(songId: SongId) {
        favoritesDao.setFavorite(songId.value, true)
    }

    override suspend fun removeFavorite(songId: SongId) {
        favoritesDao.setFavorite(songId.value, false)
    }

    override suspend fun toggleFavorite(songId: SongId): Boolean {
        val current = favoritesDao.isFavorite(songId.value)
        if (current == null) {
            return false
        }
        val newValue = !current
        favoritesDao.setFavorite(songId.value, newValue)
        return newValue
    }
}
