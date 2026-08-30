package com.example.comicbookreader.core.data.mapper

import com.example.comicbookreader.core.database.entity.ComicEntity
import com.example.comicbookreader.core.domain.model.Comic

fun ComicEntity.toComic(): Comic = Comic(
    id = id,
    title = title,
    pageCount = pageCount,
    currentPage = currentPage,
    lastOpened = lastOpened,
    dateAdded = dateAdded,
    fileUri = fileUri,
    thumbnailPath = thumbnailPath,
    folderPath = folderPath
)

fun Comic.toComicEntity(): ComicEntity = ComicEntity(
    id = id,
    title = title,
    pageCount = pageCount,
    currentPage = currentPage,
    lastOpened = lastOpened,
    dateAdded = dateAdded,
    fileUri = fileUri,
    thumbnailPath = thumbnailPath,
    folderPath = folderPath
)