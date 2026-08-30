package com.example.comicbookreader.feature.readinglists.presentation.editcontents

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

class ReadingListEditContentsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var readingListRepository: FakeReadingListRepository
    private lateinit var comicRepository: FakeComicRepository
    private lateinit var viewModel: ReadingListEditContentsViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        readingListRepository = FakeReadingListRepository()
        comicRepository = FakeComicRepository()
        viewModel = ReadingListEditContentsViewModel(readingListRepository, comicRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun seedThreeItemList() {
        comicRepository.seedComic(testComic(1))
        comicRepository.seedComic(testComic(2))
        comicRepository.seedComic(testComic(3))
        readingListRepository.seedList(ReadingList(id = 1, name = "List", createdAt = LocalDateTime.now()))
        readingListRepository.seedItem(ReadingListItem(id = 1, listId = 1, comicId = 1, position = 0))
        readingListRepository.seedItem(ReadingListItem(id = 2, listId = 1, comicId = 2, position = 1))
        readingListRepository.seedItem(ReadingListItem(id = 3, listId = 1, comicId = 3, position = 2))
    }

    @Test
    fun `reordering an adjacent swap renumbers positions without leaving gaps`() = runTest {
        seedThreeItemList()

        viewModel.load(1)
        viewModel.onAction(ReadingListEditContentsAction.OnItemsReordered(orderedItemIds = listOf(1, 3, 2)))

        assertThat(viewModel.state.value.items.map { it.comicId }).containsExactly(1, 3, 2)
    }

    @Test
    fun `reordering a non-adjacent permutation persists the full new order`() = runTest {
        seedThreeItemList()

        viewModel.load(1)
        viewModel.onAction(ReadingListEditContentsAction.OnItemsReordered(orderedItemIds = listOf(3, 1, 2)))

        assertThat(viewModel.state.value.items.map { it.comicId }).containsExactly(3, 1, 2)
    }

    @Test
    fun `removing an item then reordering leaves no gaps`() = runTest {
        seedThreeItemList()

        viewModel.load(1)
        viewModel.onAction(ReadingListEditContentsAction.OnRemoveItemClick(itemId = 2))
        viewModel.onAction(ReadingListEditContentsAction.OnItemsReordered(orderedItemIds = listOf(3, 1)))

        assertThat(viewModel.state.value.items.map { it.comicId }).containsExactly(3, 1)
    }

    @Test
    fun `add comics click emits navigate to picker for the current list`() = runTest {
        seedThreeItemList()
        viewModel.load(1)

        viewModel.events.test {
            viewModel.onAction(ReadingListEditContentsAction.OnAddComicsClick)
            assertThat(awaitItem()).isEqualTo(ReadingListEditContentsEvent.NavigateToComicPicker(listId = 1))
        }
    }
}