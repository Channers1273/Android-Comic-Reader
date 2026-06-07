package com.example.comicbookreader.di

import android.content.Context
import androidx.room.Room
import com.example.comicbookreader.data.local.ComicDao
import com.example.comicbookreader.data.local.ComicDatabase
import com.example.comicbookreader.data.repository.ComicRepositoryImpl
import com.example.comicbookreader.data.repository.UserSettingsRepositoryImpl
import com.example.comicbookreader.domain.repository.ComicRepository
import com.example.comicbookreader.domain.repository.UserSettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideComicDatabase(@ApplicationContext context: Context): ComicDatabase {
        return Room.databaseBuilder(
            context,
            ComicDatabase::class.java,
            "comics.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideComicDao(database: ComicDatabase): ComicDao {
        return database.dao
    }

    @Provides
    @Singleton
    fun provideComicRepository(dao: ComicDao, @ApplicationContext context: Context): ComicRepository {
        return ComicRepositoryImpl(dao, context)
    }

    @Provides
    @Singleton
    fun provideUserSettingsRepository(@ApplicationContext context: Context): UserSettingsRepository {
        return UserSettingsRepositoryImpl(context)
    }
}