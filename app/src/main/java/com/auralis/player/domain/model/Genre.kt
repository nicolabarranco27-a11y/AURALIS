package com.auralis.player.domain.model

data class Genre(
    val id: GenreId,
    val name: String,
    val songCount: Int = 0,
)
