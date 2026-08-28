package com.auralis.player.data.metadata

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.auralis.player.core.common.dispatchers.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementacion con la API nativa de Android.
 * Se ejecuta siempre en Dispatchers.IO y libera el retriever via try/finally.
 * Los fallos de lectura no se ocultan: se propagan al llamador
 * (el futuro scanner decidira si omite el archivo o reintenta).
 */
class MediaMetadataRetrieverExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MetadataExtractor {

    override suspend fun extract(uri: Uri): TrackMetadata = withContext(ioDispatcher) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            TrackMetadata(
                title = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                artist = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                album = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                albumArtist = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST),
                genre = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_GENRE),
                year = retriever.intMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR),
                trackNumber = retriever.intMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER),
                discNumber = retriever.intMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER),
                durationMs = retriever.longMetadataOrNull(MediaMetadataRetriever.METADATA_KEY_DURATION),
                embeddedArtwork = runCatching { retriever.embeddedPicture }.getOrNull(),
            )
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun MediaMetadataRetriever.metadata(key: Int): String? =
        runCatching { extractMetadata(key) }.getOrNull()?.takeIf { it.isNotBlank() }

    private fun MediaMetadataRetriever.intMetadata(key: Int): Int? =
        metadata(key)?.toIntOrNull()?.takeIf { it > 0 }

    private fun MediaMetadataRetriever.longMetadataOrNull(key: Int): Long? =
        metadata(key)?.toLongOrNull()?.takeIf { it > 0 }
}
