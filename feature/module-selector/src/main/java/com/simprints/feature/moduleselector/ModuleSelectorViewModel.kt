package com.simprints.feature.moduleselector

import androidx.lifecycle.viewModelScope
import com.simprints.core.ExternalScope
import com.simprints.feature.moduleselector.arch.MviViewModel
import com.simprints.feature.moduleselector.model.ModuleSelectorDataState
import com.simprints.feature.moduleselector.model.ModuleSelectorUiModel
import com.simprints.feature.moduleselector.model.ModuleSelectorUiModelMapper
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.SettingsPasswordConfig
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
internal class ModuleSelectorViewModel @Inject constructor(
    private val moduleRepository: ModuleSelectionRepository,
    private val syncOrchestrator: SyncOrchestrator,
    private val configRepository: ConfigRepository,
    tokenizationProcessor: TokenizationProcessor,
    @param:ExternalScope private val externalScope: CoroutineScope,
) : MviViewModel<ModuleSelectorAction, ModuleSelectorDataState, ModuleSelectorUiModel, ModuleSelectorEffects>(
        initialDataState = ModuleSelectorDataState(),
        mapper = ModuleSelectorUiModelMapper(tokenizationProcessor),
    ) {
    init {
        viewModelScope.launch {
            val maxModules = moduleRepository.getMaxNumberOfModules()
            val password = configRepository.getProjectConfiguration().general.settingsPassword
            updateState { it.copy(maxNumberOfModules = maxModules, settingsPassword = password) }

            configRepository.getProject()?.let { project ->
                val modules = moduleRepository.getModules()
                updateState { it.copy(allModules = modules, project = project) }
            }
        }
    }

    override suspend fun processAction(action: ModuleSelectorAction) {
        when (action) {
            ModuleSelectorAction.LockOverlayClicked ->
                state.settingsPassword
                    .getNullablePassword()
                    ?.let { password -> sendEffect(ModuleSelectorEffects.ShowPassword(password)) }
            ModuleSelectorAction.UnlockScreen ->
                updateState { it.copy(settingsPassword = SettingsPasswordConfig.Unlocked) }
            is ModuleSelectorAction.SearchQueryChanged ->
                updateState { it.copy(query = action.query) }
            is ModuleSelectorAction.OnlySelectedChanged ->
                updateState { it.copy(onlySelected = action.enabled) }
            is ModuleSelectorAction.ModuleClicked ->
                updateState { state ->
                    state.copy(
                        allModules = state.allModules.map { module ->
                            if (module.name == action.module.tokenizedName) module.copy(isSelected = !module.isSelected) else module
                        },
                    )
                }
            ModuleSelectorAction.CancelClicked ->
                sendEffect(ModuleSelectorEffects.Dismiss)
            ModuleSelectorAction.SaveClicked ->
                saveAndDismiss()
        }
    }

    private fun saveAndDismiss() {
        externalScope.launch {
            moduleRepository.saveModules(
                state.allModules.map { SelectableModule(it.name, it.isSelected) },
            )
            syncOrchestrator.execute(OneTime.Events.restart())
        }
        sendEffect(ModuleSelectorEffects.Dismiss)
    }
}
