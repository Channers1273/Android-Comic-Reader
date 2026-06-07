package com.example.comicbookreader.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    val comicDirectory: Flow<String>
    suspend fun saveComicDirectory(directory: String)
}