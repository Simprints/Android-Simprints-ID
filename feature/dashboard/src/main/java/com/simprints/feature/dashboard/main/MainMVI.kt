package com.simprints.feature.dashboard.main

internal data class MainUiState(
    // null: not loaded yet, the privacy notice menu item keeps its XML default (visible).
    val isConsentRequired: Boolean? = null,
    // Sticky: once detected, the overlay stays for the ViewModel's lifetime, including view re-creation. Never an effect.
    val isRootedDeviceDetected: Boolean = false,
)

internal sealed interface MainUiAction {
    data object Load : MainUiAction
}

internal sealed interface MainUiEffect
