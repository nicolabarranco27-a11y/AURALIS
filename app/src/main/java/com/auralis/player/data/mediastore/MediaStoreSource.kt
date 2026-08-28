package com.auralis.player.data.mediastore

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.auralis.player.core.common.dispatchers.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Cancion descubierta en MediaStore, antes de enriquecerse con metadatos.
 */
data class MediaStoreAudio(
    val mediaStoreId: Long,
    val uri: String,
    val displayName: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val albumArtist: String?,
    val year: Int?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val durationMs: Long?,
    val dateAddedEpochMs: Long?,
    val dateModifiedEpochMs: Long?,
    val mimeType: String?,
    val sizeBytes: Long?,
    val path: String?,
)

/**
 * Fuente de descubrimiento del catalogo.
 * Room sera la fuente de verdad; MediaStore solo aporta los datos descubiertos.
 * La API queda lista para que el futuro LibraryScanner la consuma.
 */
interface AudioDiscoverySource {
    suspend fun getAudioFiles(): List<MediaStoreAudio>
}

class MediaStoreSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AudioDiscoverySource {

    override suspend fun getAudioFiles(): List<MediaStoreAudio> = withContext(ioDispatcher) {
        queryAudio()
    }

    private fun queryAudio(): List<MediaStoreAudio> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        // ALBUM_ARTIST solo existe como columna desde API 30.
        val includeAlbumArtist = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.DISPLAY_NAME)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            if (includeAlbumArtist) add(MediaStore.Audio.Media.ALBUM_ARTIST)
            add(MediaStore.Audio.Media.YEAR)
            add(MediaStore.Audio.Media.TRACK)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.DATE_ADDED)
            add(MediaStore.Audio.Media.DATE_MODIFIED)
            add(MediaStore.Audio.Media.MIME_TYPE)
            add(MediaStore.Audio.Media.SIZE)
            add(MediaStore.Audio.Media.DATA)
        }.toTypedArray()

        val result = mutableListOf<MediaStoreAudio>()

        context.contentResolver.query(
            collection,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC",
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val displayIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val titleIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumArtistIdx = if (includeAlbumArtist) {
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)
            } else {
                -1
            }
            val yearIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val trackIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val durationIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val addedIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val modifiedIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val mimeIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val sizeIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val pathIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIdx)
                val trackRaw = cursor.getInt(trackIdx)
                result += MediaStoreAudio(
                    mediaStoreId = id,
                    uri = Uri.withAppendedPath(collection, id.toString()).toString(),
                    displayName = cursor.getString(displayIdx),
                    title = cursor.getString(titleIdx),
                    artist = cursor.getString(artistIdx),
                    album = cursor.getString(albumIdx),
                    albumArtist = if (albumArtistIdx >= 0) cursor.getString(albumArtistIdx) else null,
                    year = cursor.getIntOrNull(yearIdx),
                    trackNumber = decodeTrackNumber(trackRaw),
                    discNumber = decodeDiscNumber(trackRaw),
                    durationMs = cursor.getLong(durationIdx).takeIf { it > 0 },
                    dateAddedEpochMs = cursor.getLong(addedIdx).takeIf { it > 0 }?.times(1000L),
                    dateModifiedEpochMs = cursor.getLong(modifiedIdx).takeIf { it > 0 }?.times(1000L),
                    mimeType = cursor.getString(mimeIdx),
                    sizeBytes = cursor.getLong(sizeIdx).takeIf { it > 0 },
                    path = cursor.getString(pathIdx),
                )
            }
        }

        return result
    }

    /** TRACK usa el formato cdddttt (disco + pista). */
    private fun decodeTrackNumber(raw: Int): Int? =
        raw.takeIf { it > 0 }?.let { it % TRACK_MODULUS }

    private fun decodeDiscNumber(raw: Int): Int? =
        raw.takeIf { it >= TRACK_MODULUS }?.let { it / TRACK_MODULUS }

    private companion object {
        const val TRACK_MODULUS = 1000
    }
}

private fun android.database.Cursor.getIntOrNull(index: Int): Int? {
    if (index < 0 || isNull(index)) return null
    return getInt(index).takeIf { it != 0 }
}
