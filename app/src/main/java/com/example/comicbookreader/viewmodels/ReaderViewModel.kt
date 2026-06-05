package com.example.comicbookreader.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.comicbookreader.data.repository.ComicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//class ReaderViewModelFactory(private val comicRepository: ComicRepository)

class ReaderViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ComicRepository()
    private val _comicPages = MutableStateFlow<List<Uri>>(emptyList())

    val pages = _comicPages.asStateFlow()


    fun loadComic(comicUri: Uri) {
        viewModelScope.launch{
            withContext(Dispatchers.Default) {
                // Todo: Put in Try-Catch Block
                repository.unzipComic(getApplication(), comicUri = comicUri).collect { uri ->
                    _comicPages.update { currentList ->
                        currentList + uri
                    }

                }
            }

        }

    }
}