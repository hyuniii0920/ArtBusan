package com.example.artbusan.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artbusan.data.Museum
import com.example.artbusan.data.MuseumRepository
import kotlinx.coroutines.launch

class MuseumViewModel(private val repo: MuseumRepository) : ViewModel() {

    val museums = MutableLiveData<List<Museum>>()

    fun load(category: String? = null) {
        viewModelScope.launch {
            museums.value = if (category == null) repo.getAll()
                            else repo.getByCategory(category)
        }
    }
}
