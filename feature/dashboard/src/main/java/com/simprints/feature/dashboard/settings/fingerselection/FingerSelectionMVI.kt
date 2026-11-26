package com.simprints.feature.dashboard.settings.fingerselection

internal data class FingerSelectionUiState(
    // null: not loaded yet, the list keeps its empty XML default.
    val sections: List<FingerSelectionSection>? = null,
)

internal sealed interface FingerSelectionUiAction {
    data object Load : FingerSelectionUiAction
}

internal sealed interface FingerSelectionUiEffect
