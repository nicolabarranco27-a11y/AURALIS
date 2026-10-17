// Copyright (c) 2025-2026 EstalingradoCorp. All rights reserved.
// Licensed under the PolyForm Noncommercial License 1.0.0.
// https://polyformproject.org/licenses/noncommercial/1.0.0

package com.auralis.player.data.artwork

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import com.auralis.player.core.common.color.dominantColorFromPixels
import com.auralis.player.core.common.dispatchers.DefaultDispatcher
import com.auralis.player.core.common.dispatchers.IoDispatcher
import com.auralis.player.domain.repository.ArtworkColorRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Extrae el color dominante de la caratula decodificando una miniatura del
 * bitmap. La decodificacion (IO) y el analisis (CPU) corren fuera del hilo
 * principal y el resultado se cachea por referencia.
 */
@Singleton
class MediaArtworkColorRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ArtworkColorRepository {

    private val cache = object : LruCache<String, Int>(MAX_CACHE_ENTRIES) {}

    override suspend fun dominantColorFor(artworkReference: String?): Int? {
        val reference = artworkReference?.trim().orEmpty()
        if (reference.isEmpty()) return null

        synchronized(cache) { cache[reference] }?.let { cached ->
            return cached.takeIf { it != NO_COLOR }
        }

        val pixels = withContext(ioDispatcher) { decodePixels(reference) }
        val argb = pixels
            ?.let { withContext(defaultDispatcher) { dominantColorFromPixels(it)?.toArgb() } }
            ?: NO_COLOR

        synchronized(cache) { cache.put(reference, argb) }
        return argb.takeIf { it != NO_COLOR }
    }

    private fun decodePixels(reference: String): IntArray? {
        val uri = toUri(reference) ?: return null
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        runCatching {
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        }.getOrNull()
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = runCatching {
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOptions) }
        }.getOrNull() ?: return null

        return try {
            val width = bitmap.width
            val height = bitmap.height
            if (width <= 0 || height <= 0) return null
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            pixels
        } finally {
            bitmap.recycle()
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int): Int {
        var sample = 1
        var longest = maxOf(width, height)
        while (longest / 2 >= TARGET_SAMPLE_SIZE) {
            longest /= 2
            sample *= 2
        }
        return sample
    }

    private fun toUri(reference: String): Uri? = when {
        reference.contains("://") -> Uri.parse(reference)
        reference.startsWith("/") -> Uri.fromFile(File(reference))
        else -> Uri.parse(reference)
    }

    private companion object {
        const val TARGET_SAMPLE_SIZE = 64
        const val MAX_CACHE_ENTRIES = 64
        const val NO_COLOR = 0
    }
}
