package com.auralis.player.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.auralis.player.data.database.dao.FavoritesDao
import com.auralis.player.data.database.dao.LibraryDao
import com.auralis.player.data.database.dao.PlaylistDao
import com.auralis.player.data.database.entity.AlbumEntity
import com.auralis.player.data.database.entity.ArtistEntity
import com.auralis.player.data.database.entity.GenreEntity
import com.auralis.player.data.database.entity.PlaylistEntity
import com.auralis.player.data.database.entity.PlaylistSongEntity
import com.auralis.player.data.database.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        GenreEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoritesDao(): FavoritesDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE songs ADD COLUMN isAvailable INTEGER NOT NULL DEFAULT 1",
                )
            }
        }
    }
}
