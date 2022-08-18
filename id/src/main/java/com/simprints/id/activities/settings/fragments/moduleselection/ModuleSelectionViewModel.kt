package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.services.sync.events.master.EventSyncManager
import kotlinx.coroutines.launch

class ModuleSelectionViewModel(
    private val repository: ModuleRepository,
    private val eventSyncManager: EventSyncManager
) : ViewModel() {

    val modulesList = MutableLiveData<List<Module>>().apply {
        value = repository.getModules()
    }

    fun updateModules(modules: List<Module>) {
        modulesList.value = modules
    }

    fun saveModules(modules: List<Module>) {
        viewModelScope.launch {
            repository.saveModules(modules)
            syncNewModules()
        }
    }

    private fun syncNewModules() {
        with(eventSyncManager) {
            stop()
            sync()
        }
    }

    fun resetModules() {
        modulesList.value = repository.getModules()
    }

    fun getMaxNumberOfModules(): Int = repository.getMaxNumberOfModules()

}
