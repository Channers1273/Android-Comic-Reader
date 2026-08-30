package com.example.comicbookreader.core.di

import android.content.Context
import androidx.room.Room
import com.example.comicbookreader.core.database.ComicBookDatabase
import com.example.comicbookreader.core.database.dao.ComicDao
import com.example.comicbookreader.core.database.dao.ReadingListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideComicBookDatabase(@ApplicationContext context: Context): ComicBookDatabase {
        return Room.databaseBuilder(
            context,
            ComicBookDatabase::class.java,
            "comics.db"
        )
            // Pre-release app, no real user data to preserve yet. Replace with real
            // Migration/autoMigration objects before any beta/release build.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideComicDao(database: ComicBookDatabase): ComicDao {
        return database.comicDao
    }

    @Provides
    @Singleton
    fun provideReadingListDao(database: ComicBookDatabase): ReadingListDao {
        return database.readingListDao
    }
}