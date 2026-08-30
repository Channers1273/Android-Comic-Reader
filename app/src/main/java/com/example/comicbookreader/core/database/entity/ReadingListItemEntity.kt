package com.example.comicbookreader.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_list_item",
    foreignKeys = [
        ForeignKey(
            entity = ReadingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ComicEntity::class,
            parentColumns = ["id"],
            childColumns = ["comicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("listId"),
        Index("comicId"),
        Index(value = ["listId", "comicId"], unique = true)
    ]
)
data class ReadingListItemEntity(
    val listId: Long,
    val comicId: Int,
    val position: Int,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)