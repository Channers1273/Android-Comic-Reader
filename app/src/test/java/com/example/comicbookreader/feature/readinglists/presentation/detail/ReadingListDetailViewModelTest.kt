package com.example.comicbookreader.feature.readinglists.presentation.detail

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.example.comicbookreader.core.domain.model.ReadingList
import com.example.comicbookreader.core.domain.model.ReadingListItem
import com.example.comicbookreader.fakes.FakeComicRepository
import com.example.comicbookreader.fakes.FakeReadingListRepository
import com.example.comicbookreader.fakes.testComic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.time.LocalDateTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ReadingListDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var readingListRepository: FakeReadingListRepository
    private lateinit var comicRepository: FakeComicRepository
    private lateinit var viewModel: ReadingListDetailViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        readingListRepository = FakeReadingListRepository()
        comicRepository = FakeComicRepository()
        viewModel = ReadingListDetailViewModel(readingListRepository, comicRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load maps items to ui models in position order`() = runTest {
        comicRepository.seedComic(testComic(1))
        comicRepository.seedComic(testComic(2))
        readingListRepository.seedList(ReadingList(id = 1, name = "List", createdAt = LocalDateTime.now()))
        readingListRepository.seedItem(ReadingListItem(id = 1, listId = 1, comicId = 1, position = 0))
        readingListRepository.seedItem(ReadingListItem(id = 2, listId = 1, comicId = 2, position = 1))

        viewModel.load(1)

        assertThat(viewModel.state.value.items.map { it.comicId }).containsExactly(1, 2)
    }

    @Test
    fun `item click emits navigate to reader with the current list id`() = runTest {
        comicRepository.seedComic(testComic(1))
        readingListRepository.seedList(ReadingList(id = 1, name = "List", createdAt = LocalDateTime.now()))
        readingListRepository.seedItem(ReadingListItem(id = 1, listId = 1, comicId = 1, position = 0))

        viewModel.load(1)

        viewModel.events.test {
            viewModel.onAction(ReadingListDetailAction.OnItemClick(comicId = 1))
            assertThat(awaitItem()).isEqualTo(ReadingListDetailEvent.NavigateToReader(comicId = 1, listId = 1))
        }
    }
}