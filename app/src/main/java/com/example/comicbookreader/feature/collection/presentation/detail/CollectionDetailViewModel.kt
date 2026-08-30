package com.example.comicbookreader.feature.collection.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comicbookreader.core.domain.repository.ComicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    private val comicRepository: ComicRepository
) : ViewModel() {

    private val eventChannel = Channel<CollectionDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(CollectionDetailState())
    val state = _state.asStateFlow()

    private var loadJob: Job? = null

    fun load(folderPath: String) {
        if (_state.value.folderPath == folderPath && loadJob != null) return

        loadJob?.cancel()
        _state.value = CollectionDetailState(folderPath = folderPath)
        loadJob = comicRepository.getAllComics()
            .map { comics -> comics.filter { it.folderPath == folderPath } }
            .onEach { comics -> _state.update { it.copy(comics = comics) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: CollectionDetailAction) {
        when (action) {
            is CollectionDetailAction.OnComicClick -> {
                viewModelScope.launch {
                    eventChannel.send(CollectionDetailEvent.NavigateToReader(action.comicId))
                }
            }
        }
    }
}