package com.example.comicbookreader.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comicbookreader.domain.repository.ComicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val comicRepository: ComicRepository,
): ViewModel() {

    // YOU'RE TRYING TO NAVIGATE collection screen to see stuff in the db
    private val _comicPages = MutableStateFlow<List<Uri>>(emptyList())
    val pages = _comicPages.asStateFlow()

    fun loadComic(comicUri: Uri) {
        viewModelScope.launch{
            // Todo: Put in Try-Catch Block
            comicRepository.unzipComic(comicUri = comicUri).collect { uri ->
                _comicPages.update { currentList ->
                    currentList + uri
                }
            }
        }
    }
}