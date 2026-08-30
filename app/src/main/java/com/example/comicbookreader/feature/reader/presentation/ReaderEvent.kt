package com.example.comicbookreader.feature.reader.presentation

sealed interface ReaderEvent {
    data class NavigateToNextComic(val comicId: Int, val listId: Long) : ReaderEvent
}