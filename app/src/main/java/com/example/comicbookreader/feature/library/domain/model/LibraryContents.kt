package com.example.comicbookreader.feature.library.domain.model

import com.example.comicbookreader.core.domain.model.Comic
import com.example.comicbookreader.core.domain.model.ComicCollection

data class LibraryContents(
    val folders: List<ComicCollection> = emptyList(),
    val rootComics: List<Comic> = emptyList()
)