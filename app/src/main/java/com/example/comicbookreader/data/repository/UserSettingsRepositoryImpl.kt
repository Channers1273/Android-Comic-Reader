package com.example.comicbookreader.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.comicbookreader.data.local.dataStore
import com.example.comicbookreader.domain.repository.UserSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserSettingsRepositoryImpl(private val context: Context): UserSettingsRepository {

    private object Keys {
        val COMIC_DIRECTORY = stringPreferencesKey("comicDirectory")
    }

    override val comicDirectory: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.COMIC_DIRECTORY] ?: ""
        }

    override suspend fun saveComicDirectory(directory: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.COMIC_DIRECTORY] = directory
        }
    }
}