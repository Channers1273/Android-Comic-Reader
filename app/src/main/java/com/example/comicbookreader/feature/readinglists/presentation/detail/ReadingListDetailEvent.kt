package com.example.comicbookreader.feature.readinglists.presentation.detail

sealed interface ReadingListDetailEvent {
    data class NavigateToReader(val comicId: Int, val listId: Long) : ReadingListDetailEvent
}