package com.example.comicbookreader.feature.reader.domain.usecase

import com.example.comicbookreader.core.domain.model.Comic
import com.example.comicbookreader.core.domain.repository.ComicRepository
import com.example.comicbookreader.core.domain.repository.ReadingListRepository
import javax.inject.Inject

class GetNextComicInListUseCase @Inject constructor(
    private val readingListRepository: ReadingListRepository,
    private val comicRepository: ComicRepository
) {
    suspend operator fun invoke(listId: Long, currentComicId: Int): Comic? {
        val nextItem = readingListRepository.getNextItemInList(listId, currentComicId) ?: return null
        return comicRepository.getComicById(nextItem.comicId)
    }
}