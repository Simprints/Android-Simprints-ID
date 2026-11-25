package com.simprints.feature.dashboard.logout.syncdecline

internal data class LogoutSyncDeclineUiState(
    // State, not an effect, so the dialog comes back after view re-creation (rotation).
    val isLogoutConfirmationVisible: Boolean = false,
)

internal sealed interface LogoutSyncDeclineUiAction {
    // Reads the password configuration freshly on every click.
    data object ConfirmClicked : LogoutSyncDeclineUiAction

    // Confirmation dialog positive button, or password dialog success.
    data object LogoutConfirmed : LogoutSyncDeclineUiAction

    // Confirmation dialog cancelled by the user (cancel button, back or a touch outside).
    data object LogoutConfirmationDismissed : LogoutSyncDeclineUiAction
}

internal sealed interface LogoutSyncDeclineUiEffect {
    data class ShowPasswordDialog(
        val password: String,
    ) : LogoutSyncDeclineUiEffect
}
