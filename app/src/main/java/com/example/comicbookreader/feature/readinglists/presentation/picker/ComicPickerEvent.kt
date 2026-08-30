package com.example.comicbookreader.feature.readinglists.presentation.picker

sealed interface ComicPickerEvent {
    data object NavigateBack : ComicPickerEvent
}