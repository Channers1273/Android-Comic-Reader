package com.example.comicbookreader.feature.reader.domain.usecase

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.example.comicbookreader.core.domain.model.ReadingListItem
import com.example.comicbookreader.fakes.FakeComicRepository
import com.example.comicbookreader.fakes.FakeReadingListRepository
import com.example.comicbookreader.fakes.testComic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetNextComicInListUseCaseTest {

    private lateinit var comicRepository: FakeComicRepository
    private lateinit var readingListRepository: FakeReadingListRepository
    private lateinit var useCase: GetNextComicInListUseCase

    @BeforeEach
    fun setUp() {
        comicRepository = FakeComicRepository()
        readingListRepository = FakeReadingListRepository()
        useCase = GetNextComicInListUseCase(readingListRepository, comicRepository)
    }

    @Test
    fun `returns the next comic when current comic is mid-list`() = runTest {
        comicRepository.seedComic(testComic(1))
        comicRepository.seedComic(testComic(2))
        comicRepository.seedComic(testComic(3))
        readingListRepository.seedItem(ReadingListItem(id = 1, listId = 10, comicId = 1, position = 0))
        readingListRepository.seedItem(ReadingListItem(id = 2, listId = 10, comicId = 2, position = 1))
        readingListRepository.seedItem(ReadingListItem(id = 3, listId = 10, comicId = 3, position = 2))

        val result = useCase(listId = 10, currentComicId = 2)

        assertThat(result?.id).isEqualTo(3)
    }

    @Test
    fun `returns null at the last item in the list`() = runTest {
        comicRepository.seedComic(testComic(1))
        readingListRepository.seedItem(ReadingListItem(id = 1, listId = 10, comicId = 1, position = 0))

        val result = useCase(listId = 10, currentComicId = 1)

        assertThat(result).isNull()
    }

    @Test
    fun `returns null when the comic is not in the list`() = runTest {
        comicRepository.seedComic(testComic(99))

        val result = useCase(listId = 10, currentComicId = 99)

        assertThat(result).isNull()
    }
}