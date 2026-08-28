package com.auralis.player.data.scanner

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.auralis.player.core.common.dispatchers.IoDispatcher
import com.auralis.player.data.database.dao.LibraryDao
import com.auralis.player.data.database.mapper.toEntity
import com.auralis.player.data.mediastore.AudioDiscoverySource
import com.auralis.player.data.metadata.MetadataExtractor
import com.auralis.player.domain.model.Song
import com.auralis.player.domain.model.SongId
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

interface LibraryScanner {
    suspend fun sync(): ScanOutcome
}

/**
 * Sincronizacion incremental Room <-> MediaStore.
 * - Solo reprocesa canciones nuevas o modificadas (uri/dateModified/size).
 * - Eliminacion logica: las desaparecidas se marcan isAvailable = 0,
 *   conservando playlists, favoritos e historico.
 * - Fallos puntuales de metadata no abortan el escaneo (fallback MediaStore).
 * - Cancelacion: CancellationException siempre se re-lanza.
 */
@Singleton
class DefaultLibraryScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val discoverySource: AudioDiscoverySource,
    private val libraryDao: LibraryDao,
    private val metadataExtractor: MetadataExtractor,
    private val scanMonitor: LibraryScanMonitor,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LibraryScanner {

    override suspend fun sync(): ScanOutcome {
        scanMonitor.onScanStarted()
        return try {
            val outcome = withContext(ioDispatcher) { performSync() }
            scanMonitor.onScanFinished(outcome)
            outcome
        } catch (cancellation: CancellationException) {
            scanMonitor.onScanAborted()
            throw cancellation
        } catch (t: Throwable) {
            val outcome = ScanOutcome.Failed(t.message ?: t.javaClass.simpleName)
            scanMonitor.onScanFinished(outcome)
            outcome
        }
    }

    private suspend fun performSync(): ScanOutcome {
        if (!context.hasAudioAccess()) {
            return ScanOutcome.PermissionDenied
        }

        val discovered = discoverySource.getAudioFiles()
        val existingById = libraryDao.getAllIncludingUnavailable()
            .associateBy { it.mediaStoreId }

        var added = 0
        var updated = 0
        val upsertBuffer = mutableListOf<com.auralis.player.data.database.entity.SongEntity>()
        val seenSongIds = HashSet<String>(discovered.size)

        for (audio in discovered) {
            currentCoroutineContext().ensureActive()

            seenSongIds += songIdFor(audio.mediaStoreId)
            val existing = existingById[audio.mediaStoreId]
            // Una cancion no disponible que reaparece debe restaurarse,
            // aunque su contenido no haya cambiado.
            if (existing != null && existing.isAvailable && !hasChanged(existing, audio)) continue

            var entity = buildSong(audio)
                .toEntity(
                    mediaStoreId = audio.mediaStoreId,
                    mimeType = audio.mimeType,
                    sizeBytes = audio.sizeBytes,
                    path = audio.path,
                    dateModifiedEpochMs = audio.dateModifiedEpochMs,
                )
                .copy(isAvailable = true)

            if (existing != null) {
                entity = entity.copy(
                    playCount = existing.playCount,
                    lastPlayedAtEpochMs = existing.lastPlayedAtEpochMs,
                    isFavorite = existing.isFavorite,
                )
                updated++
            } else {
                added++
            }

            upsertBuffer += entity
            if (upsertBuffer.size >= BATCH_SIZE) {
                libraryDao.upsertAll(upsertBuffer.toList())
                upsertBuffer.clear()
            }
        }

        libraryDao.upsertAll(upsertBuffer.toList())

        val removed = if (existingById.isEmpty() && discovered.isNotEmpty()) {
            0
        } else {
            libraryDao.markUnavailableExcept(seenSongIds.toList())
        }

        return ScanOutcome.Success(
            scanned = discovered.size,
            added = added,
            updated = updated,
            removed = removed,
        )
    }

    private fun hasChanged(
        existing: com.auralis.player.data.database.entity.SongEntity,
        audio: com.auralis.player.data.mediastore.MediaStoreAudio,
    ): Boolean =
        existing.uri != audio.uri ||
            existing.dateModifiedEpochMs != audio.dateModifiedEpochMs ||
            existing.sizeBytes != audio.sizeBytes

    /** MediaStore provee la base; el extractor solo completa o corrige. */
    private suspend fun buildSong(audio: com.auralis.player.data.mediastore.MediaStoreAudio): Song {
        val base = Song(
            id = SongId(songIdFor(audio.mediaStoreId)),
            sourceUri = audio.uri,
            title = audio.title ?: audio.displayName,
            artist = cleanUnknown(audio.artist),
            album = cleanUnknown(audio.album),
            albumArtist = cleanUnknown(audio.albumArtist),
            genre = null,
            year = audio.year,
            trackNumber = audio.trackNumber,
            discNumber = audio.discNumber,
            durationMs = audio.durationMs,
            dateAdded = audio.dateAddedEpochMs?.let(Instant::ofEpochMilli),
        )

        return try {
            val metadata = metadataExtractor.extract(audio.uri.toUri())
            base.copy(
                artist = metadata.artist ?: base.artist,
                album = metadata.album ?: base.album,
                albumArtist = metadata.albumArtist ?: base.albumArtist,
                genre = metadata.genre ?: base.genre,
                year = metadata.year ?: base.year,
                trackNumber = metadata.trackNumber ?: base.trackNumber,
                discNumber = metadata.discNumber ?: base.discNumber,
                durationMs = metadata.durationMs ?: base.durationMs,
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (t: Throwable) {
            base
        }
    }

    companion object {
        /** Identidad estable: el id de dominio deriva del mediaStoreId. */
        fun songIdFor(mediaStoreId: Long): String = mediaStoreId.toString()

        const val BATCH_SIZE = 500

        /** Marcador que Android usa para artistas/albumes desconocidos. */
        const val UNKNOWN_MARKER = "<unknown>"

        private fun cleanUnknown(value: String?): String? =
            value?.takeIf { it.isNotBlank() && !it.equals(UNKNOWN_MARKER, ignoreCase = true) }
    }
}

internal fun Context.hasAudioAccess(): Boolean {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_AUDIO
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }
    return ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED
}
