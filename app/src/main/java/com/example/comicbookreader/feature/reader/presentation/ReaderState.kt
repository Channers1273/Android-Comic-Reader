package com.example.comicbookreader.feature.reader.presentation

import com.example.comicbookreader.core.presentation.UiText

data class ReaderState(
    val comicId: Int = 0,
    val title: String = "",
    val pageCount: Int = 0,
    val pages: List<String> = emptyList(),
    val currentPageIndex: Int = 0,
    val isOverlayVisible: Boolean = false,
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val listContext: ReadingListContext = ReadingListContext.Standalone
)