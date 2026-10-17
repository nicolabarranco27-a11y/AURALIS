// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.di

import android.content.Context
import androidx.room.Room
import com.auralis.player.data.database.AppDatabase
import com.auralis.player.data.database.dao.FavoritesDao
import com.auralis.player.data.database.dao.LibraryDao
import com.auralis.player.data.database.dao.PlaylistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "auralis.db"

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
            )
            .build()

    @Provides
    fun provideLibraryDao(database: AppDatabase): LibraryDao = database.libraryDao()

    @Provides
    fun providePlaylistDao(database: AppDatabase): PlaylistDao = database.playlistDao()

    @Provides
    fun provideFavoritesDao(database: AppDatabase): FavoritesDao = database.favoritesDao()
}
