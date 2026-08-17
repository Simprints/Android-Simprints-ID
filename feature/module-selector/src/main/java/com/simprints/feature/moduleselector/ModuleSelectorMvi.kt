package com.simprints.feature.moduleselector

import com.simprints.feature.moduleselector.adapter.ModuleSelectorItem

internal data class ModuleSelectorState(
    val modules: List<ModuleSelectorItem> = emptyList(),
    val query: String = "",
    val onlySelected: Boolean = false,
    val isConfirmEnabled: Boolean = false,
    val selectionError: SelectionError? = null,
    val isScreenLocked: Boolean = false,
) {
    internal sealed interface SelectionError {
        data object NoModuleSelected : SelectionError

        data class TooManyModulesSelected(
            val maxCount: Int,
        ) : SelectionError
    }
}

internal sealed interface ModuleSelectorEffects {
    data object Dismiss : ModuleSelectorEffects

    data class ShowPassword(
        val password: String,
    ) : ModuleSelectorEffects
}

internal sealed interface ModuleSelectorAction {
    data object LockOverlayClicked : ModuleSelectorAction

    data object UnlockScreen : ModuleSelectorAction

    data class SearchQueryChanged(
        val query: String,
    ) : ModuleSelectorAction

    data class OnlySelectedChanged(
        val enabled: Boolean,
    ) : ModuleSelectorAction

    data class ModuleClicked(
        val module: ModuleSelectorItem.Module,
    ) : ModuleSelectorAction

    data object CancelClicked : ModuleSelectorAction

    data object SaveClicked : ModuleSelectorAction
}
