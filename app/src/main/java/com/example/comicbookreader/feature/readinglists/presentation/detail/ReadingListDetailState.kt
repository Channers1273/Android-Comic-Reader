package com.example.comicbookreader.feature.readinglists.presentation.detail

data class ReadingListDetailState(
    val listId: Long = 0,
    val items: List<ReadingListItemUi> = emptyList()
)

data class ReadingListItemUi(
    val itemId: Long,
    val comicId: Int,
    val title: String,
    val thumbnailPath: String?
)