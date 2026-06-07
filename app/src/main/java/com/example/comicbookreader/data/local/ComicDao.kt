package com.example.comicbookreader.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.comicbookreader.data.model.Comic

@Dao
interface ComicDao {

    @Upsert
    suspend fun upsertComic(comic: Comic)

    @Query("SELECT * FROM comic")
    suspend fun getAllComics(): List<Comic>


}