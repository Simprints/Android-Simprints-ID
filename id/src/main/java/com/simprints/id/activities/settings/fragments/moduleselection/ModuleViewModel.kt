package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.model.Module
import kotlinx.coroutines.launch

class ModuleViewModel(private val repository: ModuleRepository) : ViewModel() {

    val modulesList = MutableLiveData<List<Module>>().apply {
        value = repository.getModules()
    }

    fun updateModules(modules: List<Module>) {
        modulesList.value = modules
    }

    fun saveModules(modules: List<Module>) {
        viewModelScope.launch {
            repository.saveModules(modules)
        }
    }

    fun resetModules() {
        modulesList.value = repository.getModules()
    }

    fun getMaxNumberOfModules(): Int = repository.getMaxNumberOfModules()

}
