package com.example.comicbookreader.feature.library.presentation

import com.example.comicbookreader.core.domain.model.Comic
import com.example.comicbookreader.core.domain.model.ComicCollection

data class LibraryState(
    val comicDirectory: String? = null,
    val folders: List<ComicCollection> = emptyList(),
    val rootComics: List<Comic> = emptyList(),
    val isSyncing: Boolean = false
)