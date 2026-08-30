package com.example.comicbookreader.fakes

import com.example.comicbookreader.core.domain.model.Comic

fun testComic(
    id: Int,
    folderPath: String = "",
    pageCount: Int = 2,
    currentPage: Int = 0
): Comic = Comic(
    id = id,
    title = "Comic $id",
    pageCount = pageCount,
    currentPage = currentPage,
    lastOpened = null,
    dateAdded = null,
    fileUri = "content://comic$id.cbz",
    thumbnailPath = null,
    folderPath = folderPath
)