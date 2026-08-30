package com.example.comicbookreader.feature.readinglists.presentation.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comicbookreader.core.domain.repository.ComicRepository
import com.example.comicbookreader.core.domain.repository.ReadingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComicPickerViewModel @Inject constructor(
    private val readingListRepository: ReadingListRepository,
    private val comicRepository: ComicRepository
) : ViewModel() {

    private val eventChannel = Channel<ComicPickerEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ComicPickerState())
    val state = _state.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private val selectedComicIds = MutableStateFlow<Set<Int>>(emptySet())

    private var loadJob: Job? = null

    fun load(listId: Long) {
        if (_state.value.listId == listId && loadJob != null) return

        loadJob?.cancel()
        _state.value = ComicPickerState(listId = listId)
        loadJob = combine(
            readingListRepository.getItemsForList(listId),
            comicRepository.getAllComics(),
            searchQuery,
            selectedComicIds
        ) { items, comics, query, selectedIds ->
            val alreadyInList = items.map { it.comicId }.toSet()
            val available = comics.filterNot { it.id in alreadyInList }
            val filtered = if (query.isBlank()) {
                available
            } else {
                available.filter { it.title.contains(query, ignoreCase = true) }
            }
            query to filtered.map { comic ->
                SelectableComicUiModel(comic = comic, isSelected = comic.id in selectedIds)
            }
        }.onEach { (query, comics) ->
            _state.update { it.copy(searchQuery = query, comics = comics) }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: ComicPickerAction) {
        when (action) {
            is ComicPickerAction.OnSearchQueryChanged -> {
                searchQuery.value = action.query
            }

            is ComicPickerAction.OnComicToggled -> {
                selectedComicIds.update { ids ->
                    if (action.comicId in ids) ids - action.comicId else ids + action.comicId
                }
            }

            ComicPickerAction.OnDoneClick -> {
                viewModelScope.launch {
                    val listId = _state.value.listId
                    selectedComicIds.value.forEach { comicId ->
                        readingListRepository.addComicToList(listId, comicId)
                    }
                    eventChannel.send(ComicPickerEvent.NavigateBack)
                }
            }

            ComicPickerAction.OnBackClick -> {
                viewModelScope.launch {
                    eventChannel.send(ComicPickerEvent.NavigateBack)
                }
            }
        }
    }
}