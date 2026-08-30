package com.example.comicbookreader.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.comicbookreader.core.database.converters.LocalDateTimeConverter
import com.example.comicbookreader.core.database.dao.ComicDao
import com.example.comicbookreader.core.database.dao.ReadingListDao
import com.example.comicbookreader.core.database.entity.ComicEntity
import com.example.comicbookreader.core.database.entity.ReadingListEntity
import com.example.comicbookreader.core.database.entity.ReadingListItemEntity

@Database(
    entities = [ComicEntity::class, ReadingListEntity::class, ReadingListItemEntity::class],
    version = 6
)
@TypeConverters(LocalDateTimeConverter::class)
abstract class ComicBookDatabase : RoomDatabase() {

    abstract val comicDao: ComicDao
    abstract val readingListDao: ReadingListDao
}