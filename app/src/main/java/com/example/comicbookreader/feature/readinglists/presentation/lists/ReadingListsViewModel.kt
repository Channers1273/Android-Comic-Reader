package com.example.comicbookreader.feature.readinglists.presentation.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comicbookreader.core.domain.repository.ReadingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingListsViewModel @Inject constructor(
    private val readingListRepository: ReadingListRepository
) : ViewModel() {

    private val eventChannel = Channel<ReadingListsEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ReadingListsState())
    val state = _state.asStateFlow()

    init {
        readingListRepository.getAllLists()
            .onEach { lists -> _state.update { it.copy(lists = lists) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: ReadingListsAction) {
        when (action) {
            is ReadingListsAction.OnListClick -> {
                viewModelScope.launch {
                    eventChannel.send(ReadingListsEvent.NavigateToListDetail(action.listId))
                }
            }

            is ReadingListsAction.OnMenuClick -> {
                _state.update { it.copy(menuOpenForListId = action.listId) }
            }

            ReadingListsAction.OnMenuDismiss -> {
                _state.update { it.copy(menuOpenForListId = null) }
            }

            is ReadingListsAction.OnRenameMenuItemClick -> {
                _state.update { it.copy(menuOpenForListId = null, listPendingRename = action.list) }
            }

            is ReadingListsAction.OnEditContentsMenuItemClick -> {
                _state.update { it.copy(menuOpenForListId = null) }
                viewModelScope.launch {
                    eventChannel.send(ReadingListsEvent.NavigateToEditContents(action.listId))
                }
            }

            is ReadingListsAction.OnDeleteMenuItemClick -> {
                _state.update { it.copy(menuOpenForListId = null, listPendingDelete = action.list) }
            }

            ReadingListsAction.OnAddListClick -> {
                _state.update { it.copy(showCreateDialog = true) }
            }

            ReadingListsAction.OnCreateDialogDismiss -> {
                _state.update { it.copy(showCreateDialog = false) }
            }

            is ReadingListsAction.OnCreateListConfirm -> {
                val name = action.name.trim()
                if (name.isNotEmpty()) {
                    viewModelScope.launch {
                        val id = readingListRepository.createList(name)
                        _state.update { it.copy(showCreateDialog = false) }
                        eventChannel.send(ReadingListsEvent.NavigateToComicPicker(id))
                    }
                }
            }

            is ReadingListsAction.OnRenameConfirm -> {
                val name = action.newName.trim()
                if (name.isNotEmpty()) {
                    viewModelScope.launch {
                        readingListRepository.renameList(action.listId, name)
                        _state.update { it.copy(listPendingRename = null) }
                    }
                }
            }

            ReadingListsAction.OnRenameDialogDismiss -> {
                _state.update { it.copy(listPendingRename = null) }
            }

            is ReadingListsAction.OnDeleteConfirm -> {
                viewModelScope.launch {
                    readingListRepository.deleteList(action.listId)
                    _state.update { it.copy(listPendingDelete = null) }
                }
            }

            ReadingListsAction.OnDeleteDialogDismiss -> {
                _state.update { it.copy(listPendingDelete = null) }
            }
        }
    }
}