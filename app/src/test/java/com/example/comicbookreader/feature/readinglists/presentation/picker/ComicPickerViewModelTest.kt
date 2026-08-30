package com.example.comicbookreader.feature.readinglists.presentation.picker

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.example.comicbookreader.core.domain.model.Comic
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

class ComicPickerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var readingListRepository: FakeReadingListRepository
    private lateinit var comicRepository: FakeComicRepository
    private lateinit var viewModel: ComicPickerViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        readingListRepository = FakeReadingListRepository()
        comicRepository = FakeComicRepository()
        viewModel = ComicPickerViewModel(readingListRepository, comicRepository)
        readingListRepository.seedList(ReadingList(id = 1, name = "List", createdAt = LocalDateTime.now()))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun Comic.withTitle(title: String) = copy(title = title)

    @Test
    fun `comics already in the list are excluded`() = runTest {
        comicRepository.seedComic(testComic(1))
        comicRepository.seedComic(testComic(2))
        readingListRepository.seedItem(ReadingListItem(id = 1, listId = 1, comicId = 1, position = 0))

        viewModel.load(1)

        assertThat(viewModel.state.value.comics.map { it.comic.id }).containsExactly(2)
    }

    @Test
    fun `search filters by title substring case-insensitively`() = runTest {
        comicRepository.seedComic(testComic(1).withTitle("Batman Year One"))
        comicRepository.seedComic(testComic(2).withTitle("Superman Red Son"))

        viewModel.load(1)
        viewModel.onAction(ComicPickerAction.OnSearchQueryChanged("batman"))

        assertThat(viewModel.state.value.comics.map { it.comic.id }).containsExactly(1)
    }

    @Test
    fun `toggling a comic only flips that comic's selection`() = runTest {
        comicRepository.seedComic(testComic(1))
        comicRepository.seedComic(testComic(2))

        viewModel.load(1)
        viewModel.onAction(ComicPickerAction.OnComicToggled(comicId = 1))

        val comics = viewModel.state.value.comics
        assertThat(comics.first { it.comic.id == 1 }.isSelected).isTrue()
        assertThat(comics.first { it.comic.id == 2 }.isSelected).isFalse()
    }

    @Test
    fun `done adds only selected comics and navigates back`() = runTest {
        comicRepository.seedComic(testComic(1))
        comicRepository.seedComic(testComic(2))

        viewModel.load(1)
        viewModel.onAction(ComicPickerAction.OnComicToggled(comicId = 1))

        viewModel.events.test {
            viewModel.onAction(ComicPickerAction.OnDoneClick)
            assertThat(awaitItem()).isEqualTo(ComicPickerEvent.NavigateBack)
        }

        // Comic 1 was selected and added, so it's no longer "available"; comic 2 was
        // never selected, so it was never added and remains available.
        assertThat(viewModel.state.value.comics.map { it.comic.id }).containsExactly(2)
    }
}