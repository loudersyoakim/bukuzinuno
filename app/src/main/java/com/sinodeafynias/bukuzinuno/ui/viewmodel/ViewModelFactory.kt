package com.sinodeafynias.bukuzinuno.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sinodeafynias.bukuzinuno.data.repository.LaguRepository

class ViewModelFactory(private val repository: LaguRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LaguViewModel::class.java)) {
            return LaguViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}