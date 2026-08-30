package com.example.comicbookreader.core.domain.model

data class ReadingListItem(
    val id: Long = 0,
    val listId: Long,
    val comicId: Int,
    val position: Int
)