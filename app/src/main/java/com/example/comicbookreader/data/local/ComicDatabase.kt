package com.example.comicbookreader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.comicbookreader.data.model.Comic


@Database(
    entities = [Comic::class],
    version = 1
)
abstract class ComicDatabase: RoomDatabase() {

    abstract val dao: ComicDao
}