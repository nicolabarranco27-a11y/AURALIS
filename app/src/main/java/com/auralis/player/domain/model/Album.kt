package com.auralis.player.domain.model

data class Album(
    val id: AlbumId,
    val title: String,
    val artist: String? = null,
    val year: Int? = null,
    val coverReference: String? = null,
    val songCount: Int = 0,
)
