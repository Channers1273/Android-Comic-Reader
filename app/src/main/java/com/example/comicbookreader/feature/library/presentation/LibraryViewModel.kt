package com.example.comicbookreader.feature.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comicbookreader.core.domain.error.onFailure
import com.example.comicbookreader.core.domain.repository.ComicRepository
import com.example.comicbookreader.core.domain.repository.UserSettingsRepository
import com.example.comicbookreader.core.presentation.mapper.toUiText
import com.example.comicbookreader.feature.library.domain.usecase.GetLibraryContentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
class LibraryViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val comicRepository: ComicRepository,
    private val getLibraryContentsUseCase: GetLibraryContentsUseCase
) : ViewModel() {

    private val eventChannel = Channel<LibraryEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(LibraryState())
    val state = _state.asStateFlow()

    init {
        combine(
            userSettingsRepository.comicDirectory,
            getLibraryContentsUseCase()
        ) { directory, contents -> directory to contents }
            .onEach { (directory, contents) ->
                _state.update {
                    it.copy(
                        comicDirectory = directory,
                        folders = contents.folders,
                        rootComics = contents.rootComics
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: LibraryAction) {
        when (action) {
            LibraryAction.OnChangeDirectoryClick -> {
                viewModelScope.launch { eventChannel.send(LibraryEvent.RequestDirectoryPicker) }
            }

            is LibraryAction.OnDirectoryPicked -> {
                viewModelScope.launch {
                    userSettingsRepository.saveComicDirectory(action.uriString)
                    _state.update { it.copy(isSyncing = true) }
                    comicRepository.syncLibrary(action.uriString)
                        .onFailure { error -> eventChannel.send(LibraryEvent.ShowError(error.toUiText())) }
                    _state.update { it.copy(isSyncing = false) }
                }
            }

            is LibraryAction.OnFolderClick -> {
                viewModelScope.launch { eventChannel.send(LibraryEvent.NavigateToFolder(action.folderPath)) }
            }

            is LibraryAction.OnComicClick -> {
                viewModelScope.launch { eventChannel.send(LibraryEvent.NavigateToReader(action.comicId)) }
            }

            LibraryAction.OnRefresh -> {
                viewModelScope.launch {
                    val directory = _state.value.comicDirectory ?: return@launch
                    _state.update { it.copy(isSyncing = true) }
                    comicRepository.syncLibrary(directory)
                        .onFailure { error -> eventChannel.send(LibraryEvent.ShowError(error.toUiText())) }
                    _state.update { it.copy(isSyncing = false) }
                }
            }
        }
    }
}