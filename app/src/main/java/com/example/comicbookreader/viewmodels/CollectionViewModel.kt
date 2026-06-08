package com.example.comicbookreader.viewmodels

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.room.Database
import androidx.room.Room
import com.example.comicbookreader.data.local.ComicDao
import com.example.comicbookreader.data.local.ComicDatabase
import com.example.comicbookreader.data.model.Comic
import com.example.comicbookreader.domain.repository.ComicRepository
import com.example.comicbookreader.domain.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val comicRepository: ComicRepository,
    private val userSettingsRepository: UserSettingsRepository
): ViewModel() {

    init {
        viewModelScope.launch {
            userSettingsRepository.comicDirectory.collect { folderUriString ->
                folderUriString?.let {
                    comicRepository.syncLibrary(folderUriString.toUri())
                }
            }
        }
    }

    val comics = comicRepository.getAllComics()
        .stateIn (
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

}