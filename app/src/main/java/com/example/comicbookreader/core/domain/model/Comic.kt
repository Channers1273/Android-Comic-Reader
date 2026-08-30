package com.example.comicbookreader.core.domain.model

import java.time.LocalDateTime

data class Comic(
    val id: Int = 0,
    val title: String,
    val pageCount: Int,
    val currentPage: Int = 0,
    val lastOpened: LocalDateTime?,
    val dateAdded: LocalDateTime?,
    val fileUri: String,
    val thumbnailPath: String?,
    val folderPath: String
)