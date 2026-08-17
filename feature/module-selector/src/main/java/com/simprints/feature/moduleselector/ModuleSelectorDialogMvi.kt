package com.simprints.feature.moduleselector

import com.simprints.feature.moduleselector.adapter.ModuleSelectorItem

internal data class ModuleSelectorDialogState(
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

internal sealed interface ModuleSelectorDialogEffects {
    data object DismissDialog : ModuleSelectorDialogEffects

    data class ShowPasswordDialog(
        val password: String,
    ) : ModuleSelectorDialogEffects
}

internal sealed interface ModuleSelectorDialogAction {
    data object LockOverlayClicked : ModuleSelectorDialogAction

    data object UnlockScreen : ModuleSelectorDialogAction

    data class SearchQueryChanged(
        val query: String,
    ) : ModuleSelectorDialogAction

    data class OnlySelectedChanged(
        val enabled: Boolean,
    ) : ModuleSelectorDialogAction

    data class ModuleClicked(
        val module: ModuleSelectorItem.Module,
    ) : ModuleSelectorDialogAction

    data object CancelClicked : ModuleSelectorDialogAction

    data object SaveClicked : ModuleSelectorDialogAction
}
