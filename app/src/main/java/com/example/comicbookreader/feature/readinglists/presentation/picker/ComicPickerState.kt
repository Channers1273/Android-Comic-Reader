package com.example.comicbookreader.feature.readinglists.presentation.picker

import com.example.comicbookreader.core.domain.model.Comic

data class ComicPickerState(
    val listId: Long = 0,
    val searchQuery: String = "",
    val comics: List<SelectableComicUiModel> = emptyList()
)

data class SelectableComicUiModel(
    val comic: Comic,
    val isSelected: Boolean
)