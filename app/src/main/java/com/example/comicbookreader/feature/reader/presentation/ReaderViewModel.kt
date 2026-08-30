package com.example.comicbookreader.feature.reader.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comicbookreader.R
import com.example.comicbookreader.core.domain.error.onFailure
import com.example.comicbookreader.core.domain.error.onSuccess
import com.example.comicbookreader.core.domain.repository.ComicRepository
import com.example.comicbookreader.core.presentation.UiText
import com.example.comicbookreader.core.presentation.mapper.toUiText
import com.example.comicbookreader.feature.reader.domain.usecase.GetNextComicInListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val comicRepository: ComicRepository,
    private val getNextComicInListUseCase: GetNextComicInListUseCase
) : ViewModel() {

    private val eventChannel = Channel<ReaderEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ReaderState())
    val state = _state.asStateFlow()

    private var loadJob: Job? = null

    fun loadComic(comicId: Int, readingListId: Long?) {
        if (_state.value.comicId == comicId && loadJob != null) return

        loadJob?.cancel()
        _state.value = ReaderState(comicId = comicId, isLoading = true)
        loadJob = viewModelScope.launch {
            val comic = comicRepository.getComicById(comicId)
            if (comic == null) {
                _state.update {
                    it.copy(isLoading = false, error = UiText.StringResource(R.string.error_comic_not_found))
                }
                return@launch
            }

            _state.update {
                it.copy(
                    title = comic.title,
                    pageCount = comic.pageCount,
                    currentPageIndex = comic.currentPage.coerceIn(0, (comic.pageCount - 1).coerceAtLeast(0))
                )
            }

            if (readingListId != null) {
                val nextComic = getNextComicInListUseCase(readingListId, comicId)
                _state.update { it.copy(listContext = ReadingListContext.InList(readingListId, nextComic)) }
            }

            comicRepository.unzipComic(comic.id, comic.fileUri).collect { result ->
                result
                    .onSuccess { pageUri ->
                        _state.update { it.copy(pages = it.pages + pageUri, isLoading = false) }
                    }
                    .onFailure { error ->
                        _state.update { it.copy(isLoading = false, error = error.toUiText()) }
                    }
            }
        }
    }

    fun onAction(action: ReaderAction) {
        when (action) {
            is ReaderAction.OnPageChanged -> {
                _state.update { it.copy(currentPageIndex = action.index) }
                viewModelScope.launch {
                    comicRepository.updateReadingProgress(_state.value.comicId, action.index)
                }
            }

            ReaderAction.OnToggleOverlay -> {
                _state.update { it.copy(isOverlayVisible = !it.isOverlayVisible) }
            }

            ReaderAction.OnContinueToNextComic -> {
                val listContext = _state.value.listContext
                if (listContext is ReadingListContext.InList && listContext.nextComic != null) {
                    viewModelScope.launch {
                        eventChannel.send(
                            ReaderEvent.NavigateToNextComic(listContext.nextComic.id, listContext.listId)
                        )
                    }
                }
            }
        }
    }
}