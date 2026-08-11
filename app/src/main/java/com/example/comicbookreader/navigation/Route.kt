package com.example.comicbookreader.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object Library : Route

    @Serializable
    data object Collections : Route

    @Serializable
    data class CollectionDetail(val folderPath: String) : Route

    @Serializable
    data object ReadingLists : Route

    @Serializable
    data class ReadingListDetail(val listId: Long) : Route

    @Serializable
    data class Reader(val comicId: Int, val readingListId: Long? = null) : Route
}