package com.example.comicbookreader.feature.reader.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import com.example.comicbookreader.core.domain.error.DataError
import com.example.comicbookreader.core.domain.error.Result
import com.example.comicbookreader.core.domain.model.ReadingList
import com.example.comicbookreader.core.domain.model.ReadingListItem
import com.example.comicbookreader.fakes.FakeComicRepository
import com.example.comicbookreader.fakes.FakeReadingListRepository
import com.example.comicbookreader.fakes.testComic
import com.example.comicbookreader.feature.reader.domain.usecase.GetNextComicInListUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.time.LocalDateTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ReaderViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var comicRepository: FakeComicRepository
    private lateinit var readingListRepository: FakeReadingListRepository
    private lateinit var viewModel: ReaderViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        comicRepository = FakeComicRepository()
        readingListRepository = FakeReadingListRepository()
        viewModel = ReaderViewModel(
            comicRepository = comicRepository,
            getNextComicInListUseCase = GetNextComicInListUseCase(readingListRepository, comicRepository)
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `changing page persists reading progress`() = runTest {
        comicRepository.seedComic(testComic(1))
        comicRepository.unzipResults = listOf(Result.Success("page1"), Result.Success("page2"))

        viewModel.loadComic(comicId = 1, readingListId = null)
        viewModel.onAction(ReaderAction.OnPageChanged(1))

        assertThat(comicRepository.progressUpdates).contains(1 to 1)
    }

    @Test
    fun `loading a comic restores the previously saved page`() = runTest {
        comicRepository.seedComic(testComic(1, pageCount = 5, currentPage = 3))
        comicRepository.unzipResults = List(5) { Result.Success("page${it + 1}") }

        viewModel.loadComic(comicId = 1, readingListId = null)

        assertThat(viewModel.state.value.currentPageIndex).isEqualTo(3)
    }

    @Test
    fun `corrupted archive surfaces as an error instead of crashing`() = runTest {
        comicRepository.seedComic(testComic(1))
        comicRepository.unzipResults = listOf(Result.Error(DataError.Local.CORRUPTED_ARCHIVE))

        viewModel.loadComic(comicId = 1, readingListId = null)

        assertThat(viewModel.state.value.error).isNotNull()
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `standalone comic never looks up a next comic`() = runTest {
        comicRepository.seedComic(testComic(1))
        comicRepository.unzipResults = listOf(Result.Success("page1"))

        viewModel.loadComic(comicId = 1, readingListId = null)

        assertThat(viewModel.state.value.listContext).isEqualTo(ReadingListContext.Standalone)
    }

    @Test
    fun `reaching the end of a comic in a reading list emits NavigateToNextComic`() = runTest {
        comicRepository.seedComic(testComic(1))
        comicRepository.seedComic(testComic(2))
        comicRepository.unzipResults = listOf(Result.Success("page1"))
        readingListRepository.seedList(ReadingList(id = 10, name = "List", createdAt = LocalDateTime.now()))
        readingListRepository.seedItem(ReadingListItem(id = 1, listId = 10, comicId = 1, position = 0))
        readingListRepository.seedItem(ReadingListItem(id = 2, listId = 10, comicId = 2, position = 1))

        viewModel.loadComic(comicId = 1, readingListId = 10)

        viewModel.events.test {
            viewModel.onAction(ReaderAction.OnContinueToNextComic)
            assertThat(awaitItem()).isEqualTo(ReaderEvent.NavigateToNextComic(comicId = 2, listId = 10))
        }
    }
}