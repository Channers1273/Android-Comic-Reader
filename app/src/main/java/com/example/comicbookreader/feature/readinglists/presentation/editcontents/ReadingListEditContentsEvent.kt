package com.example.comicbookreader.feature.readinglists.presentation.editcontents

sealed interface ReadingListEditContentsEvent {
    data class NavigateToComicPicker(val listId: Long) : ReadingListEditContentsEvent
    data object NavigateBack : ReadingListEditContentsEvent
}