package com.dastanapps.sdk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dastanapps.hilt.MainViewModel
import javax.inject.Inject
import javax.inject.Provider

/**
 * Factory for creating ViewModels using Dagger providers.
 */
class SdkViewModelFactory @Inject constructor(
    private val mainViewModelProvider: Provider<MainViewModel>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return mainViewModelProvider.get() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
