package com.auralis.player.data.mediastore

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
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
    val albumId: Long,
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
 *
 * El escaneo por defecto cubre toda la musica del dispositivo. Las carpetas
 * elegidas por el usuario (URIs de arbol del selector de documentos / Storage
 * Access Framework) se SUMAN a esa base: se indexa su audio aunque el sistema
 * no lo marque como musica. Se descarta siempre el audio que no es musica.
 */
interface AudioDiscoverySource {
    suspend fun getAudioFiles(folderTreeUris: Set<String>): List<MediaStoreAudio>
}

class MediaStoreSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AudioDiscoverySource {

    override suspend fun getAudioFiles(folderTreeUris: Set<String>): List<MediaStoreAudio> =
        withContext(ioDispatcher) {
            queryAudio(folderTreeUris.mapNotNull(::parseTreeUri).distinct())
        }

    private fun queryAudio(folders: List<FolderFilter>): List<MediaStoreAudio> {
        // EXTERNAL_CONTENT_URI agrega todos los volumenes externos.
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        // ALBUM_ARTIST solo existe como columna desde API 30.
        val includeAlbumArtist = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        // RELATIVE_PATH existe desde API 29; en versiones previas usamos DATA,
        // que en Android 11+ y targetSdk >= 30 esta bloqueada y no puede pedirse.
        val includeRelativePath = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.ALBUM_ID)
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
            add(MediaStore.Audio.Media.IS_MUSIC)
            if (includeRelativePath) {
                add(MediaStore.Audio.Media.VOLUME_NAME)
                add(MediaStore.Audio.Media.RELATIVE_PATH)
            } else {
                add(MediaStore.Audio.Media.DATA)
            }
        }.toTypedArray()

        val result = mutableListOf<MediaStoreAudio>()

        // Escaneo por defecto: toda la musica del dispositivo (IS_MUSIC). Las
        // carpetas elegidas por el usuario se SUMAN: se incluye su audio aunque
        // el sistema no lo marque como musica (p. ej. descargas). AudioPathFilter
        // siempre descarta mensajeria, grabaciones y sonidos del sistema.
        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC",
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val albumIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
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
            val isMusicIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
            val volumeIdx = if (includeRelativePath) {
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.VOLUME_NAME)
            } else {
                -1
            }
            val pathIdx = if (includeRelativePath) {
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
            } else {
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            }

            while (cursor.moveToNext()) {
                val rawPath = cursor.safeGetString(pathIdx)
                // Descarta audio que no es musica (mensajeria, grabaciones y
                // sonidos del sistema) aunque caiga dentro de una carpeta elegida.
                if (isNonMusicAudioPath(rawPath)) continue

                val isMusic = cursor.getInt(isMusicIdx) != 0
                val inSelectedFolder = isInsideSelectedFolder(
                    rowVolume = if (volumeIdx >= 0) cursor.safeGetString(volumeIdx) else null,
                    rowPath = rawPath,
                    folders = folders,
                    includeRelativePath = includeRelativePath,
                )
                if (!isMusic && !inSelectedFolder) continue

                val id = cursor.getLong(idIdx)
                result += MediaStoreAudio(
                    mediaStoreId = id,
                    albumId = cursor.getLong(albumIdIdx),
                    uri = Uri.withAppendedPath(collection, id.toString()).toString(),
                    displayName = cursor.safeGetString(displayIdx).orEmpty(),
                    title = cursor.safeGetString(titleIdx),
                    artist = cursor.safeGetString(artistIdx),
                    album = cursor.safeGetString(albumIdx),
                    albumArtist = if (albumArtistIdx >= 0) cursor.safeGetString(albumArtistIdx) else null,
                    year = cursor.getIntOrNull(yearIdx),
                    trackNumber = decodeTrackNumber(cursor.getInt(trackIdx)),
                    discNumber = decodeDiscNumber(cursor.getInt(trackIdx)),
                    durationMs = cursor.getLong(durationIdx).takeIf { it > 0 },
                    dateAddedEpochMs = cursor.getLong(addedIdx).takeIf { it > 0 }?.times(1000L),
                    dateModifiedEpochMs = cursor.getLong(modifiedIdx).takeIf { it > 0 }?.times(1000L),
                    mimeType = cursor.safeGetString(mimeIdx),
                    sizeBytes = cursor.getLong(sizeIdx).takeIf { it > 0 },
                    path = if (includeRelativePath) null else rawPath,
                )
            }
        }

        return result
    }

    /**
     * `true` si la pista cae dentro de alguna carpeta elegida por el usuario.
     */
    private fun isInsideSelectedFolder(
        rowVolume: String?,
        rowPath: String?,
        folders: List<FolderFilter>,
        includeRelativePath: Boolean,
    ): Boolean {
        if (folders.isEmpty() || rowPath.isNullOrBlank()) return false
        return if (includeRelativePath) {
            val rowRelative = rowPath.trim('/')
            folders.any { folder ->
                mediaVolumeName(folder.volume) == rowVolume &&
                    (folder.relativePath.isEmpty() ||
                        rowRelative == folder.relativePath ||
                        rowRelative.startsWith(folder.relativePath + "/"))
            }
        } else {
            folders.any { folder -> rowPath.startsWith(absolutePrefix(folder)) }
        }
    }

    /**
     * Traduce el URI de arbol de una carpeta elegida a un filtro dentro de
     * MediaStore. El document id tiene la forma `<volumen>:<ruta relativa>`,
     * por ejemplo `primary:Music/Albums`.
     */
    private fun parseTreeUri(treeUri: String): FolderFilter? {
        val documentId = try {
            DocumentsContract.getTreeDocumentId(Uri.parse(treeUri))
        } catch (t: Throwable) {
            return null
        }
        val separator = documentId.indexOf(':')
        if (separator <= 0) return null
        val volume = documentId.substring(0, separator)
        val relativePath = documentId.substring(separator + 1).trim('/')
        if (volume.isBlank()) return null
        return FolderFilter(volume, relativePath)
    }

    /** Nombre del volumen en MediaStore a partir del volumen del tree URI. */
    private fun mediaVolumeName(volume: String): String =
        if (volume == PRIMARY_VOLUME) PRIMARY_MEDIA_VOLUME else volume

    /** Ruta absoluta de la carpeta elegida, para filtrar DATA en API < 29. */
    private fun absolutePrefix(filter: FolderFilter): String {
        val root = if (filter.volume == PRIMARY_VOLUME) {
            Environment.getExternalStorageDirectory().absolutePath
        } else {
            "/storage/${filter.volume}"
        }
        val normalizedRoot = root.trimEnd('/')
        val relative = filter.relativePath.trim('/')
        return if (relative.isEmpty()) "$normalizedRoot/" else "$normalizedRoot/$relative/"
    }

    /** TRACK usa el formato cdddttt (disco + pista). */
    private fun decodeTrackNumber(raw: Int): Int? =
        raw.takeIf { it > 0 }?.let { it % TRACK_MODULUS }

    private fun decodeDiscNumber(raw: Int): Int? =
        raw.takeIf { it >= TRACK_MODULUS }?.let { it / TRACK_MODULUS }

    private data class FolderFilter(val volume: String, val relativePath: String)

    private companion object {
        const val TRACK_MODULUS = 1000
        const val PRIMARY_VOLUME = "primary"

        /** El volumen SAF `primary` corresponde a `external_primary` en MediaStore. */
        const val PRIMARY_MEDIA_VOLUME = "external_primary"
    }
}

private fun android.database.Cursor.safeGetString(idx: Int): String? {
    if (idx < 0 || isNull(idx)) return null
    return getString(idx)
}

private fun android.database.Cursor.getIntOrNull(idx: Int): Int? {
    if (idx < 0 || isNull(idx)) return null
    return getInt(idx)
}
