package com.example.artbusan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.artbusan.data.MuseumRepository

class MuseumViewModelFactory(private val repo: MuseumRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MuseumViewModel(repo) as T
    }
}
