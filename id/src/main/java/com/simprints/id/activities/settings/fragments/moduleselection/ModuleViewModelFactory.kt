package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.moduleselection.ModuleRepository

class ModuleViewModelFactory(private val repository: ModuleRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ModuleViewModel::class.java)) {
            ModuleViewModel(repository) as T
        } else {
            throw IllegalArgumentException("ViewModel not found")
        }
    }

}
