package com.simprints.feature.dashboard.main.projectdetails

internal data class ProjectDetailsUiState(
    // null: not loaded yet, the card keeps its XML default (visible, empty).
    val isLoaded: Boolean? = null,
    val title: String = "",
    val lastUser: String = "",
    val lastScanner: String = "",
)

internal sealed interface ProjectDetailsUiAction {
    data object Load : ProjectDetailsUiAction
}

internal sealed interface ProjectDetailsUiEffect
