// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider, SingletonImageLoader.Factory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    /**
     * ImageLoader global con cache en memoria acotada al 25% y downsampling
     * al tamano de vista, para no desbordar la gama baja con caratulas grandes.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val memoryCache = MemoryCache.Builder()
            .maxSizePercent(context, 0.25)
            .build()
        return ImageLoader.Builder(context)
            .memoryCache(memoryCache)
            .crossfade(true)
            .build()
    }
}
