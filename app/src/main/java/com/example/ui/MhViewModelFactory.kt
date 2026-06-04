package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.MhRepository

class MhViewModelFactory(private val repository: MhRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MhViewModel::class.java)) {
            return MhViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
