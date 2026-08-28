package com.auralis.player.data.metadata

import android.net.Uri

/**
 * Metadatos extraidos de un archivo de audio.
 * Todo campo es opcional: los archivos pueden carecer de tags.
 */
class TrackMetadata(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val albumArtist: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val durationMs: Long? = null,
    val embeddedArtwork: ByteArray? = null,
) {

    override fun equals(other: Any?): Boolean =
        other is TrackMetadata &&
            title == other.title &&
            artist == other.artist &&
            album == other.album &&
            albumArtist == other.albumArtist &&
            genre == other.genre &&
            year == other.year &&
            trackNumber == other.trackNumber &&
            discNumber == other.discNumber &&
            durationMs == other.durationMs &&
            (embeddedArtwork?.contentEquals(other.embeddedArtwork) ?: (other.embeddedArtwork == null))

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + album.hashCode()
        result = 31 * result + albumArtist.hashCode()
        result = 31 * result + genre.hashCode()
        result = 31 * result + (year ?: 0)
        result = 31 * result + (trackNumber ?: 0)
        result = 31 * result + (discNumber ?: 0)
        result = 31 * result + durationMs.hashCode()
        result = 31 * result + (embeddedArtwork?.contentHashCode() ?: 0)
        return result
    }
}

interface MetadataExtractor {

    /** Extrae metadatos del archivo apuntado por [uri]. */
    suspend fun extract(uri: Uri): TrackMetadata
}
