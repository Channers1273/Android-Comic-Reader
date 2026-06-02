package com.example.comicbookreader.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Note: this structure may change when I flesh out the app. This is mainly for a test

class HomeViewModel: ViewModel() {
    private val _comicPages = MutableStateFlow<List<Uri>>(emptyList())
    val comicPages = _comicPages.asStateFlow()

}