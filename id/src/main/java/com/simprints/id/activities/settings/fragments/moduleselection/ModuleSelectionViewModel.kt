package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.services.sync.events.master.EventSyncManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ModuleSelectionViewModel(
    private val repository: ModuleRepository,
    private val eventSyncManager: EventSyncManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val modulesList = MutableLiveData<List<Module>>(emptyList())
    val maxNumberOfModules = MutableLiveData(0)

    init {
        viewModelScope.launch(dispatcher) {
            modulesList.postValue(repository.getModules())
            maxNumberOfModules.postValue(repository.getMaxNumberOfModules())
        }
    }

    fun updateModules(modules: List<Module>) {
        modulesList.value = modules
    }

    fun saveModules(modules: List<Module>) {
        viewModelScope.launch(dispatcher) {
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
        viewModelScope.launch(dispatcher) {
            modulesList.value = repository.getModules()
        }
    }
}
