package com.example.comicbookreader.core.di

import com.example.comicbookreader.core.data.repository.DataStoreUserSettingsRepository
import com.example.comicbookreader.core.data.repository.DefaultComicRepository
import com.example.comicbookreader.core.data.repository.DefaultReadingListRepository
import com.example.comicbookreader.core.data.source.ComicFileDataSource
import com.example.comicbookreader.core.data.source.ComicLocalDataSource
import com.example.comicbookreader.core.data.source.RoomComicDataSource
import com.example.comicbookreader.core.data.source.SafComicFileDataSource
import com.example.comicbookreader.core.domain.repository.ComicRepository
import com.example.comicbookreader.core.domain.repository.ReadingListRepository
import com.example.comicbookreader.core.domain.repository.UserSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindComicFileDataSource(impl: SafComicFileDataSource): ComicFileDataSource

    @Binds
    @Singleton
    abstract fun bindComicLocalDataSource(impl: RoomComicDataSource): ComicLocalDataSource

    @Binds
    @Singleton
    abstract fun bindComicRepository(impl: DefaultComicRepository): ComicRepository

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepository(impl: DataStoreUserSettingsRepository): UserSettingsRepository

    @Binds
    @Singleton
    abstract fun bindReadingListRepository(impl: DefaultReadingListRepository): ReadingListRepository
}