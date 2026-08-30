package com.example.comicbookreader.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    val comicDirectory: Flow<String?>
    suspend fun saveComicDirectory(directory: String)
}