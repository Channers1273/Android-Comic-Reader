package com.example.comicbookreader.data.local

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.comicbookreader.data.model.Comic
import java.time.LocalDateTime


@Database(
    entities = [Comic::class],
    version = 4
)
@TypeConverters(LocalDateTimeConverter::class, UriConverter::class)
abstract class ComicDatabase: RoomDatabase() {

    abstract val dao: ComicDao
}

class LocalDateTimeConverter {

    @TypeConverter
    fun timeToString(time: LocalDateTime): String {
        return time.toString()
    }

    @TypeConverter
    fun stringToTime(string: String): LocalDateTime {
        return LocalDateTime.parse(string)
    }
}

class UriConverter {

    @TypeConverter
    fun uriToString(uri: Uri): String {
        return uri.toString()
    }

    @TypeConverter
    fun stringToUri(string: String): Uri {
        return string.toUri()
    }
}