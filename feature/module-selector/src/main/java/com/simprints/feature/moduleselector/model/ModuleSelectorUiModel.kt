package com.simprints.feature.moduleselector.model

import com.simprints.feature.moduleselector.ModuleSelectorState.SelectionError
import com.simprints.feature.moduleselector.adapter.ModuleSelectorItem
import com.simprints.feature.moduleselector.arch.UiModel

internal data class ModuleSelectorUiModel(
    val modules: List<ModuleSelectorItem> = emptyList(),
    val query: String = "",
    val onlySelected: Boolean = false,
    val isConfirmEnabled: Boolean = false,
    val selectionError: SelectionError? = null,
    val isScreenLocked: Boolean = false,
) : UiModel
