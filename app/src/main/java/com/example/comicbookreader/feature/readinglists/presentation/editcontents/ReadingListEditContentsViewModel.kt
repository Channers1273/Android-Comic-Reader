package com.example.comicbookreader.feature.readinglists.presentation.editcontents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comicbookreader.core.domain.repository.ComicRepository
import com.example.comicbookreader.core.domain.repository.ReadingListRepository
import com.example.comicbookreader.feature.readinglists.presentation.detail.ReadingListItemUi
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
class ReadingListEditContentsViewModel @Inject constructor(
    private val readingListRepository: ReadingListRepository,
    private val comicRepository: ComicRepository
) : ViewModel() {

    private val eventChannel = Channel<ReadingListEditContentsEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ReadingListEditContentsState())
    val state = _state.asStateFlow()

    private var loadJob: Job? = null

    fun load(listId: Long) {
        if (_state.value.listId == listId && loadJob != null) return

        loadJob?.cancel()
        _state.value = ReadingListEditContentsState(listId = listId)
        loadJob = combine(
            readingListRepository.getItemsForList(listId),
            comicRepository.getAllComics()
        ) { items, comics ->
            val comicsById = comics.associateBy { it.id }
            items.mapNotNull { item ->
                comicsById[item.comicId]?.let { comic ->
                    ReadingListItemUi(
                        itemId = item.id,
                        comicId = comic.id,
                        title = comic.title,
                        thumbnailPath = comic.thumbnailPath
                    )
                }
            }
        }.onEach { listItems ->
            _state.update { it.copy(items = listItems) }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: ReadingListEditContentsAction) {
        when (action) {
            is ReadingListEditContentsAction.OnRemoveItemClick -> {
                viewModelScope.launch {
                    readingListRepository.removeItemFromList(action.itemId)
                }
            }

            is ReadingListEditContentsAction.OnItemsReordered -> {
                viewModelScope.launch {
                    readingListRepository.reorderItems(action.orderedItemIds)
                }
            }

            ReadingListEditContentsAction.OnAddComicsClick -> {
                viewModelScope.launch {
                    eventChannel.send(
                        ReadingListEditContentsEvent.NavigateToComicPicker(_state.value.listId)
                    )
                }
            }

            ReadingListEditContentsAction.OnBackClick -> {
                viewModelScope.launch {
                    eventChannel.send(ReadingListEditContentsEvent.NavigateBack)
                }
            }
        }
    }
}