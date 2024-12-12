package com.simprints.feature.dashboard.settings.syncinfo.moduleselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.ExternalScope
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.NoModuleSelectedException
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.TooManyModulesSelectedException
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.ModuleRepository
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.sync.SyncOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ModuleSelectionViewModel @Inject constructor(
    private val authStore: AuthStore,
    private val moduleRepository: ModuleRepository,
    private val syncOrchestrator: SyncOrchestrator,
    private val configManager: ConfigManager,
    private val tokenizationProcessor: TokenizationProcessor,
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
    private val _screenLocked =
        MutableLiveData<SettingsPasswordConfig>(SettingsPasswordConfig.NotSet)

    init {
        postUpdateModules {
            maxNumberOfModules = moduleRepository.getMaxNumberOfModules()
            initialModules =
                moduleRepository.getModules().map { module ->
                    val decryptedName = when (val name = module.name) {
                        is TokenizableString.Raw -> name
                        is TokenizableString.Tokenized -> tokenizationProcessor.decrypt(
                            encrypted = name,
                            tokenKeyType = TokenKeyType.ModuleId,
                            project = configManager.getProject(authStore.signedInProjectId),
                        )
                    }
                    module.copy(name = decryptedName)
                }
            addAll(initialModules.map { it.copy() })
        }
    }

    fun loadPasswordSettings() {
        viewModelScope.launch {
            configManager
                .getProjectConfiguration()
                .general
                .settingsPassword
                .let { _screenLocked.postValue(it) }
        }
    }

    fun updateModuleSelection(moduleToUpdate: Module) {
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
            val modules = modules.map { module ->
                val encryptedName = when (val name = module.name) {
                    is TokenizableString.Raw -> tokenizationProcessor.encrypt(
                        decrypted = name,
                        tokenKeyType = TokenKeyType.ModuleId,
                        project = configManager.getProject(authStore.signedInProjectId),
                    )

                    is TokenizableString.Tokenized -> name
                }
                module.copy(name = encryptedName)
            }
            moduleRepository.saveModules(modules)
            syncOrchestrator.stopEventSync()
            syncOrchestrator.startEventSync()
        }
    }

    private fun postUpdateModules(block: suspend MutableList<Module>.() -> Unit) = viewModelScope.launch {
        modules.block()
        _modulesList.postValue(modules)
    }

    private fun getSelected() = modules.filter { it.isSelected }

    fun unlockScreen() {
        _screenLocked.postValue(SettingsPasswordConfig.Unlocked)
    }
}
