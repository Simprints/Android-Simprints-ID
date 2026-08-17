package com.simprints.feature.moduleselector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.ExternalScope
import com.simprints.feature.moduleselector.ModuleSelectorDialogState.SelectionError
import com.simprints.feature.moduleselector.adapter.ModuleSelectorItem
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ModuleSelectorDialogViewModel @Inject constructor(
    private val moduleRepository: ModuleSelectionRepository,
    private val syncOrchestrator: SyncOrchestrator,
    private val configRepository: ConfigRepository,
    private val tokenizationProcessor: TokenizationProcessor,
    @param:ExternalScope private val externalScope: CoroutineScope,
) : ViewModel() {
    val state: StateFlow<ModuleSelectorDialogState>
        field = MutableStateFlow(ModuleSelectorDialogState())

    val effects: SharedFlow<ModuleSelectorDialogEffects>
        field = MutableSharedFlow<ModuleSelectorDialogEffects>(extraBufferCapacity = 1)

    private var maxNumberOfModules = 0
    private var allModules: List<ModuleSelectorItem.Module> = listOf()
    private var settingsPassword: SettingsPasswordConfig = SettingsPasswordConfig.NotSet

    init {
        viewModelScope.launch {
            maxNumberOfModules = moduleRepository.getMaxNumberOfModules()
            settingsPassword = configRepository.getProjectConfiguration().general.settingsPassword

            configRepository.getProject()?.let { project ->
                allModules = moduleRepository.getModules().map { module ->
                    val decryptedName = tokenizationProcessor.untokenizeIfNecessary(
                        tokenizableString = module.name,
                        tokenKeyType = TokenKeyType.ModuleId,
                        project = project,
                    )
                    ModuleSelectorItem.Module(
                        name = decryptedName.value,
                        tokenizedName = module.name,
                        isSelected = module.isSelected,
                    )
                }
            }
            updateStateModules(state.value.copy(isScreenLocked = settingsPassword.locked))
        }
    }

    fun onAction(action: ModuleSelectorDialogAction) = when (action) {
        ModuleSelectorDialogAction.LockOverlayClicked -> {
            settingsPassword
                .getNullablePassword()
                ?.let { passwords -> emitEffect(ModuleSelectorDialogEffects.ShowPasswordDialog(passwords)) }
        }
        ModuleSelectorDialogAction.UnlockScreen -> {
            settingsPassword = SettingsPasswordConfig.Unlocked
            updateState { it.copy(isScreenLocked = false) }
        }
        is ModuleSelectorDialogAction.OnlySelectedChanged -> setOnlySelectedFilter(action.enabled)
        is ModuleSelectorDialogAction.SearchQueryChanged -> filterModules(action.query)
        is ModuleSelectorDialogAction.ModuleClicked -> updateModuleSelection(action.module)
        ModuleSelectorDialogAction.CancelClicked -> emitEffect(ModuleSelectorDialogEffects.DismissDialog)
        ModuleSelectorDialogAction.SaveClicked -> saveModules()
    }

    private fun updateModuleSelection(moduleToUpdate: ModuleSelectorItem.Module) {
        allModules = allModules.map { module ->
            if (module.tokenizedName == moduleToUpdate.tokenizedName) {
                module.copy(isSelected = !module.isSelected)
            } else {
                module
            }
        }
        updateStateModules(state.value)
    }

    private fun filterModules(query: String) {
        updateStateModules(state.value.copy(query = query))
    }

    private fun setOnlySelectedFilter(enabled: Boolean) {
        updateStateModules(state.value.copy(onlySelected = enabled))
    }

    private fun updateStateModules(newState: ModuleSelectorDialogState) {
        val filteredModules = allModules
            .filter { !newState.onlySelected || it.isSelected }
            .filter { newState.query.isBlank() || it.name.contains(newState.query, ignoreCase = true) }
            .ifEmpty { listOf(ModuleSelectorItem.NoResult) }

        val selectedCount = allModules.count { it.isSelected }
        val selectionError = when (selectedCount) {
            0 -> SelectionError.NoModuleSelected
            in 1..maxNumberOfModules -> null
            else -> SelectionError.TooManyModulesSelected(maxNumberOfModules)
        }

        updateState {
            newState.copy(
                modules = filteredModules,
                isConfirmEnabled = selectionError == null,
                selectionError = selectionError,
            )
        }
    }

    private fun saveModules() {
        externalScope.launch {
            moduleRepository.saveModules(allModules.map { module -> SelectableModule(module.tokenizedName, module.isSelected) })
            syncOrchestrator.execute(OneTime.Events.restart())
        }
        emitEffect(ModuleSelectorDialogEffects.DismissDialog)
    }

    private fun updateState(block: (currentState: ModuleSelectorDialogState) -> ModuleSelectorDialogState) {
        state.value = block(state.value)
    }

    private fun emitEffect(effect: ModuleSelectorDialogEffects) {
        effects.tryEmit(effect)
    }
}
