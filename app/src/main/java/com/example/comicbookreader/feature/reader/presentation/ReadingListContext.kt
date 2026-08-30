package com.example.comicbookreader.feature.reader.presentation

import com.example.comicbookreader.core.domain.model.Comic

sealed interface ReadingListContext {
    data object Standalone : ReadingListContext
    data class InList(val listId: Long, val nextComic: Comic?) : ReadingListContext
}