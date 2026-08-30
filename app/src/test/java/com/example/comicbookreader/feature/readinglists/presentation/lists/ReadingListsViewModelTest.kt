package com.example.comicbookreader.feature.readinglists.presentation.lists

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.example.comicbookreader.core.domain.model.ReadingList
import com.example.comicbookreader.fakes.FakeReadingListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.time.LocalDateTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ReadingListsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var readingListRepository: FakeReadingListRepository

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        readingListRepository = FakeReadingListRepository()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // The ViewModel starts collecting `getAllLists()` in its init block, so any fake
    // data a test needs present from the start must be seeded before construction.
    private fun createViewModel() = ReadingListsViewModel(readingListRepository)

    @Test
    fun `confirming create closes the dialog, creates the list, and navigates to the picker`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ReadingListsAction.OnAddListClick)
        assertThat(viewModel.state.value.showCreateDialog).isTrue()

        viewModel.events.test {
            viewModel.onAction(ReadingListsAction.OnCreateListConfirm("My List"))
            val event = awaitItem() as ReadingListsEvent.NavigateToComicPicker
            assertThat(viewModel.state.value.lists.first { it.id == event.listId }.name)
                .isEqualTo("My List")
        }
        assertThat(viewModel.state.value.showCreateDialog).isFalse()
    }

    @Test
    fun `blank create name is rejected`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ReadingListsAction.OnAddListClick)
        viewModel.onAction(ReadingListsAction.OnCreateListConfirm("   "))

        assertThat(viewModel.state.value.lists).isEmpty()
        assertThat(viewModel.state.value.showCreateDialog).isTrue()
    }

    @Test
    fun `rename confirm renames the list and clears the pending rename`() = runTest {
        readingListRepository.seedList(ReadingList(id = 1, name = "Old Name", createdAt = LocalDateTime.now()))
        val viewModel = createViewModel()
        val list = viewModel.state.value.lists.first()

        viewModel.onAction(ReadingListsAction.OnRenameMenuItemClick(list))
        assertThat(viewModel.state.value.listPendingRename).isEqualTo(list)

        viewModel.onAction(ReadingListsAction.OnRenameConfirm(list.id, "New Name"))

        assertThat(viewModel.state.value.lists.first().name).isEqualTo("New Name")
        assertThat(viewModel.state.value.listPendingRename).isNull()
    }

    @Test
    fun `blank rename is rejected`() = runTest {
        readingListRepository.seedList(ReadingList(id = 1, name = "Old Name", createdAt = LocalDateTime.now()))
        val viewModel = createViewModel()
        val list = viewModel.state.value.lists.first()

        viewModel.onAction(ReadingListsAction.OnRenameMenuItemClick(list))
        viewModel.onAction(ReadingListsAction.OnRenameConfirm(list.id, "  "))

        assertThat(viewModel.state.value.lists.first().name).isEqualTo("Old Name")
        assertThat(viewModel.state.value.listPendingRename).isEqualTo(list)
    }

    @Test
    fun `delete menu item click opens confirmation without deleting`() = runTest {
        readingListRepository.seedList(ReadingList(id = 1, name = "List", createdAt = LocalDateTime.now()))
        val viewModel = createViewModel()
        val list = viewModel.state.value.lists.first()

        viewModel.onAction(ReadingListsAction.OnDeleteMenuItemClick(list))

        assertThat(viewModel.state.value.listPendingDelete).isEqualTo(list)
        assertThat(viewModel.state.value.lists).isEqualTo(listOf(list))
    }

    @Test
    fun `delete confirm deletes the list and clears the pending delete`() = runTest {
        readingListRepository.seedList(ReadingList(id = 1, name = "List", createdAt = LocalDateTime.now()))
        val viewModel = createViewModel()
        val list = viewModel.state.value.lists.first()

        viewModel.onAction(ReadingListsAction.OnDeleteMenuItemClick(list))
        viewModel.onAction(ReadingListsAction.OnDeleteConfirm(list.id))

        assertThat(viewModel.state.value.lists).isEmpty()
        assertThat(viewModel.state.value.listPendingDelete).isNull()
    }

    @Test
    fun `delete dialog dismiss clears the pending delete without deleting`() = runTest {
        readingListRepository.seedList(ReadingList(id = 1, name = "List", createdAt = LocalDateTime.now()))
        val viewModel = createViewModel()
        val list = viewModel.state.value.lists.first()

        viewModel.onAction(ReadingListsAction.OnDeleteMenuItemClick(list))
        viewModel.onAction(ReadingListsAction.OnDeleteDialogDismiss)

        assertThat(viewModel.state.value.listPendingDelete).isNull()
        assertThat(viewModel.state.value.lists).isEqualTo(listOf(list))
    }

    @Test
    fun `menu click and dismiss toggle the open menu`() = runTest {
        readingListRepository.seedList(ReadingList(id = 1, name = "List", createdAt = LocalDateTime.now()))
        val viewModel = createViewModel()
        val list = viewModel.state.value.lists.first()

        viewModel.onAction(ReadingListsAction.OnMenuClick(list.id))
        assertThat(viewModel.state.value.menuOpenForListId).isEqualTo(list.id)

        viewModel.onAction(ReadingListsAction.OnMenuDismiss)
        assertThat(viewModel.state.value.menuOpenForListId).isNull()
    }
}