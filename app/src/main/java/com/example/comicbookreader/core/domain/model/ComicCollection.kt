package com.example.comicbookreader.core.domain.model

data class ComicCollection(
    val folderPath: String,
    val name: String,
    val comicCount: Int,
    val coverThumbnailPath: String?
)