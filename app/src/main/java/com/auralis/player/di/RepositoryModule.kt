package com.auralis.player.di

import com.auralis.player.data.metadata.MediaMetadataRetrieverExtractor
import com.auralis.player.data.metadata.MetadataExtractor
import com.auralis.player.data.mediastore.AudioDiscoverySource
import com.auralis.player.data.mediastore.MediaStoreSource
import com.auralis.player.data.repository.InMemoryPlaybackRepository
import com.auralis.player.data.repository.RoomFavoritesRepository
import com.auralis.player.data.repository.RoomLibraryRepository
import com.auralis.player.data.repository.RoomPlaylistRepository
import com.auralis.player.data.scanner.DefaultLibraryScanner
import com.auralis.player.data.scanner.LibraryScanner
import com.auralis.player.domain.repository.FavoritesRepository
import com.auralis.player.domain.repository.LibraryRepository
import com.auralis.player.domain.repository.PlaybackRepository
import com.auralis.player.domain.repository.PlaylistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: RoomLibraryRepository): LibraryRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: RoomPlaylistRepository): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: RoomFavoritesRepository): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindPlaybackRepository(impl: com.auralis.player.data.repository.Media3PlaybackRepository): PlaybackRepository

    @Binds
    @Singleton
    abstract fun bindMetadataExtractor(
        impl: MediaMetadataRetrieverExtractor,
    ): MetadataExtractor

    @Binds
    @Singleton
    abstract fun bindAudioDiscoverySource(impl: MediaStoreSource): AudioDiscoverySource

    @Binds
    @Singleton
    abstract fun bindLibraryScanner(impl: DefaultLibraryScanner): LibraryScanner
}
