package com.simprints.feature.dashboard.settings.syncinfo.moduleselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.ExternalScope
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.NoModuleSelectedException
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.TooManyModulesSelectedException
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.eventsync.module.ModuleSelectionRepository
import com.simprints.infra.eventsync.module.SelectableModule
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ModuleSelectionViewModel @Inject constructor(
    private val moduleRepository: ModuleSelectionRepository,
    private val syncOrchestrator: SyncOrchestrator,
    private val configRepository: ConfigRepository,
    private val tokenizationProcessor: TokenizationProcessor,
    @param:ExternalScope private val externalScope: CoroutineScope,
) : ViewModel() {
    val modulesList: LiveData<List<SelectableModule>>
        get() = _modulesList
    private val _modulesList = MutableLiveData<List<SelectableModule>>()

    private var maxNumberOfModules = 0

    private var modules: MutableList<SelectableModule> = mutableListOf()
    private var initialModules: List<SelectableModule> = listOf()

    val screenLocked: LiveData<SettingsPasswordConfig>
        get() = _screenLocked
    private val _screenLocked =
        MutableLiveData<SettingsPasswordConfig>(SettingsPasswordConfig.NotSet)

    init {
        postUpdateModules {
            maxNumberOfModules = moduleRepository.getMaxNumberOfModules()
            configRepository.getProject()?.let { project ->
                initialModules = moduleRepository.getModules().map { module ->
                    val decryptedName = tokenizationProcessor.untokenizeIfNecessary(
                        tokenizableString = module.name,
                        tokenKeyType = TokenKeyType.ModuleId,
                        project = project,
                    )
                    module.copy(name = decryptedName)
                }
                addAll(initialModules.map { it.copy() })
            }
        }
    }

    fun loadPasswordSettings() {
        viewModelScope.launch {
            configRepository
                .getProjectConfiguration()
                .general
                .settingsPassword
                .let { _screenLocked.postValue(it) }
        }
    }

    fun updateModuleSelection(moduleToUpdate: SelectableModule) {
        val selectedModulesSize = getSelected().size
        if (!moduleToUpdate.isSelected && selectedModulesSize == maxNumberOfModules) {
            throw TooManyModulesSelectedException(maxNumberOfModules = maxNumberOfModules)
        }

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
        val selectedModulesSize = getSelected().size
        if (selectedModulesSize == 0) {
            throw NoModuleSelectedException()
        }

        externalScope.launch {
            configRepository.getProject()?.let { project ->
                val modules = modules.map { module ->
                    val encryptedName = tokenizationProcessor.tokenizeIfNecessary(
                        tokenizableString = module.name,
                        tokenKeyType = TokenKeyType.ModuleId,
                        project = project,
                    )
                    module.copy(name = encryptedName)
                }
                moduleRepository.saveModules(modules)
            }

            syncOrchestrator.execute(OneTime.UpSync.restart())
            syncOrchestrator.execute(OneTime.DownSync.restart())
        }
    }

    private fun postUpdateModules(block: suspend MutableList<SelectableModule>.() -> Unit) = viewModelScope.launch {
        modules.block()
        _modulesList.postValue(modules)
    }

    private fun getSelected() = modules.filter { it.isSelected }

    fun unlockScreen() {
        _screenLocked.postValue(SettingsPasswordConfig.Unlocked)
    }
}
