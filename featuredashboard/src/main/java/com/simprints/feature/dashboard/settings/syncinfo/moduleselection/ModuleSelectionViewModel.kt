package com.simprints.feature.dashboard.settings.syncinfo.moduleselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.ExternalScope
import com.simprints.feature.dashboard.main.sync.EventSyncManager
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.NoModuleSelectedException
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.TooManyModulesSelectedException
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.ModuleRepository
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.SettingsPasswordConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ModuleSelectionViewModel @Inject constructor(
    private val repository: ModuleRepository,
    private val eventSyncManager: EventSyncManager,
    private val configManager: ConfigManager,
    @ExternalScope private val externalScope: CoroutineScope,
) : ViewModel() {

    val modulesList: LiveData<List<Module>>
        get() = _modulesList
    private val _modulesList = MutableLiveData<List<Module>>()

    private var maxNumberOfModules = 0

    private var modules: MutableList<Module> = mutableListOf()
    private var initialModules: List<Module> = listOf()

    val screenLocked: LiveData<SettingsPasswordConfig>
        get() = _screenLocked
    private val _screenLocked = MutableLiveData<SettingsPasswordConfig>(SettingsPasswordConfig.NotSet)

    init {
        viewModelScope.launch {
            configManager.getProjectConfiguration()
                .general
                .settingsPassword
                .let { _screenLocked.postValue(it) }
        }

        postUpdateModules {
            maxNumberOfModules = repository.getMaxNumberOfModules()
            initialModules = repository.getModules()
            addAll(initialModules.map { it.copy() })
        }
    }

    fun updateModuleSelection(moduleToUpdate: Module) {
        val selectedModulesSize = getSelected().size
        if (moduleToUpdate.isSelected && selectedModulesSize == 1)
            throw NoModuleSelectedException()

        if (!moduleToUpdate.isSelected && selectedModulesSize == maxNumberOfModules)
            throw TooManyModulesSelectedException(maxNumberOfModules = maxNumberOfModules)

        postUpdateModules {
            forEachIndexed { index, module ->
                if (module.name == moduleToUpdate.name) {
                    this[index].isSelected = !this[index].isSelected
                }
            }
        }
    }

    fun hasSelectionChanged(): Boolean = modules != initialModules

    fun saveModules() {
        externalScope.launch {
            repository.saveModules(modules)
            eventSyncManager.stop()
            eventSyncManager.sync()
        }
    }

    private fun postUpdateModules(block: suspend MutableList<Module>.() -> Unit) =
        viewModelScope.launch {
            modules.block()
            _modulesList.postValue(modules)
        }

    private fun getSelected() = modules.filter { it.isSelected }

    fun unlockScreen() {
        _screenLocked.postValue(SettingsPasswordConfig.Unlocked)
    }
}
