package com.example.comicbookreader.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "reading_list")
data class ReadingListEntity(
    val name: String,
    val createdAt: LocalDateTime,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)