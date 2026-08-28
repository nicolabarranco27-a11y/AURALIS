package com.auralis.player.domain.model

data class Artist(
    val id: ArtistId,
    val name: String,
    val songCount: Int = 0,
    val albumCount: Int = 0,
)
