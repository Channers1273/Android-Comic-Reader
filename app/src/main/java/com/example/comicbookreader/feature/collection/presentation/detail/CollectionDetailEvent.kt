package com.example.comicbookreader.feature.collection.presentation.detail

sealed interface CollectionDetailEvent {
    data class NavigateToReader(val comicId: Int) : CollectionDetailEvent
}