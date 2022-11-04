package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherIO
import com.simprints.core.ExternalScope
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.services.sync.events.master.EventSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModuleSelectionViewModel @Inject constructor(
    private val repository: ModuleRepository,
    private val eventSyncManager: EventSyncManager,
    @ExternalScope private val externalScope: CoroutineScope,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
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
        externalScope.launch {
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
