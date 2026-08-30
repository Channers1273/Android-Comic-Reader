package com.example.comicbookreader.feature.collection.presentation.detail

import com.example.comicbookreader.core.domain.model.Comic

data class CollectionDetailState(
    val folderPath: String = "",
    val comics: List<Comic> = emptyList()
)