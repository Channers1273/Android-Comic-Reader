package com.example.comicbookreader.feature.readinglists.presentation.lists

sealed interface ReadingListsEvent {
    data class NavigateToListDetail(val listId: Long) : ReadingListsEvent
    data class NavigateToEditContents(val listId: Long) : ReadingListsEvent
    data class NavigateToComicPicker(val listId: Long) : ReadingListsEvent
}