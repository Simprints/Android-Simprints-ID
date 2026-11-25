package com.simprints.feature.dashboard.logout.sync

internal data class LogoutSyncUiState(
    // null: no value yet, the button keeps its XML default (visible) and the info text its XML default (invisible).
    val isLogoutWithoutSyncVisible: Boolean? = null,
)

// No actions: the only click on this screen is static navigation, handled by the Fragment.
internal sealed interface LogoutSyncUiAction

internal sealed interface LogoutSyncUiEffect
