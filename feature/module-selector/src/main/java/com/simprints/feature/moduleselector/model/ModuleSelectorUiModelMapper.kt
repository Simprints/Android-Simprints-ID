package com.simprints.feature.moduleselector.model

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.moduleselector.ModuleSelectorState.SelectionError
import com.simprints.feature.moduleselector.adapter.ModuleSelectorItem
import com.simprints.feature.moduleselector.arch.StateToModelMapper
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor

internal class ModuleSelectorUiModelMapper(
    private val tokenizationProcessor: TokenizationProcessor,
) : StateToModelMapper<ModuleSelectorState, ModuleSelectorUiModel> {
    private val untokenizedModulesCache = mutableMapOf<TokenizableString, String>()

    override fun mapStateToModel(state: ModuleSelectorState): ModuleSelectorUiModel {
        val filteredModules = state.allModules
            .filter { !state.onlySelected || it.isSelected }
            .filter { state.query.isBlank() || resolveDisplayName(it.name, state).contains(state.query, ignoreCase = true) }
            .map { module ->
                ModuleSelectorItem.Module(
                    name = resolveDisplayName(module.name, state),
                    tokenizedName = module.name,
                    isSelected = module.isSelected,
                )
            }.ifEmpty { listOf(ModuleSelectorItem.NoResult) }

        val selectedCount = state.allModules.count { it.isSelected }
        val selectionError: SelectionError? = when (selectedCount) {
            0 -> SelectionError.NoModuleSelected
            in 1..state.maxNumberOfModules -> null
            else -> SelectionError.TooManyModulesSelected(state.maxNumberOfModules)
        }

        return ModuleSelectorUiModel(
            modules = filteredModules,
            query = state.query,
            onlySelected = state.onlySelected,
            isConfirmEnabled = selectionError == null,
            selectionError = selectionError,
            isScreenLocked = state.settingsPassword.locked,
        )
    }

    private fun resolveDisplayName(
        tokenized: TokenizableString,
        state: ModuleSelectorState,
    ): String = untokenizedModulesCache.getOrPut(tokenized) {
        state.project
            ?.let { project ->
                tokenizationProcessor
                    .untokenizeIfNecessary(
                        tokenizableString = tokenized,
                        tokenKeyType = TokenKeyType.ModuleId,
                        project = project,
                    ).value
            }
            ?: tokenized.value
    }
}
