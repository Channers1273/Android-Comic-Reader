package com.example.comicbookreader.feature.library.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.example.comicbookreader.R
import com.example.comicbookreader.core.domain.error.DataError
import com.example.comicbookreader.core.domain.error.Result
import com.example.comicbookreader.core.presentation.UiText
import com.example.comicbookreader.fakes.FakeComicRepository
import com.example.comicbookreader.fakes.FakeUserSettingsRepository
import com.example.comicbookreader.fakes.testComic
import com.example.comicbookreader.feature.library.domain.usecase.GetLibraryContentsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LibraryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var comicRepository: FakeComicRepository
    private lateinit var userSettingsRepository: FakeUserSettingsRepository
    private lateinit var viewModel: LibraryViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        comicRepository = FakeComicRepository()
        userSettingsRepository = FakeUserSettingsRepository()
        viewModel = LibraryViewModel(
            userSettingsRepository = userSettingsRepository,
            comicRepository = comicRepository,
            getLibraryContentsUseCase = GetLibraryContentsUseCase(comicRepository)
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `picking a directory saves it and triggers a library sync`() = runTest {
        viewModel.onAction(LibraryAction.OnDirectoryPicked("content://root"))

        assertThat(viewModel.state.value.comicDirectory).isEqualTo("content://root")
        assertThat(comicRepository.syncLibraryCalls).contains("content://root")
    }

    @Test
    fun `state splits comics into folders and root comics`() = runTest {
        comicRepository.seedComic(testComic(1, folderPath = "SeriesA"))
        comicRepository.seedComic(testComic(2, folderPath = ""))

        assertThat(viewModel.state.value.folders).hasSize(1)
        assertThat(viewModel.state.value.rootComics).hasSize(1)
    }

    @Test
    fun `clicking a folder emits NavigateToFolder`() = runTest {
        viewModel.events.test {
            viewModel.onAction(LibraryAction.OnFolderClick("SeriesA"))
            assertThat(awaitItem()).isEqualTo(LibraryEvent.NavigateToFolder("SeriesA"))
        }
    }

    @Test
    fun `clicking a root comic emits NavigateToReader`() = runTest {
        viewModel.events.test {
            viewModel.onAction(LibraryAction.OnComicClick(1))
            assertThat(awaitItem()).isEqualTo(LibraryEvent.NavigateToReader(1))
        }
    }

    @Test
    fun `pulling to refresh triggers a library sync for the stored directory`() = runTest {
        viewModel.onAction(LibraryAction.OnDirectoryPicked("content://root"))
        comicRepository.syncLibraryCalls.clear()

        viewModel.onAction(LibraryAction.OnRefresh)

        assertThat(comicRepository.syncLibraryCalls).contains("content://root")
    }

    @Test
    fun `refresh failure emits ShowError`() = runTest {
        viewModel.onAction(LibraryAction.OnDirectoryPicked("content://root"))
        comicRepository.syncLibraryResult = Result.Error(DataError.Local.PERMISSION_DENIED)

        viewModel.events.test {
            viewModel.onAction(LibraryAction.OnRefresh)
            assertThat(awaitItem()).isEqualTo(
                LibraryEvent.ShowError(UiText.StringResource(R.string.error_permission_denied))
            )
        }
    }

    @Test
    fun `refresh with no directory does nothing`() = runTest {
        comicRepository.syncLibraryCalls.clear()

        viewModel.onAction(LibraryAction.OnRefresh)

        assertThat(comicRepository.syncLibraryCalls).hasSize(0)
    }
}