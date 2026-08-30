package com.example.comicbookreader.fakes

import com.example.comicbookreader.core.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserSettingsRepository : UserSettingsRepository {

    private val directory = MutableStateFlow<String?>(null)

    override val comicDirectory: Flow<String?> = directory

    override suspend fun saveComicDirectory(directory: String) {
        this.directory.value = directory
    }
}