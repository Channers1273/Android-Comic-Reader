package com.example.comicbookreader.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route: NavKey {

    @Serializable
    data object Library: Route, NavKey {}

    @Serializable
    data class Reader(val comicUri: String): Route, NavKey

    @Serializable
    data object Collection: Route, NavKey
}