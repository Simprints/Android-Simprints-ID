package com.simprints.feature.dashboard.settings.about

import com.simprints.infra.config.store.models.SettingsPasswordConfig

internal data class AboutUiState(
    // null: not loaded yet, the sync & search summary keeps its XML default (none).
    val syncAndSearchConfig: SyncAndSearchConfig? = null,
    // Fingerprint modality configured. null: not loaded yet, the scanner version preference keeps its XML default (hidden).
    val isScannerVersionVisible: Boolean? = null,
    // null: not loaded yet, the scanner version summary keeps its XML default (none).
    val lastScannerVersion: String? = null,
    val passwordConfig: SettingsPasswordConfig = SettingsPasswordConfig.NotSet,
    // State, not an effect, so the dialog comes back after view re-creation (rotation).
    val isLogoutConfirmationVisible: Boolean = false,
)

// Actions that sit behind the settings password. The key is passed through SettingsPasswordDialogFragment and back.
internal enum class AboutGatedAction(
    val key: String,
) {
    LOGOUT("logout"),
    TROUBLESHOOTING("troubleshooting"),
    ;

    companion object {
        fun fromKey(key: String?): AboutGatedAction? = entries.firstOrNull { it.key == key }
    }
}

internal sealed interface AboutUiAction {
    // Sent once, from the ViewModel's init.
    data object Load : AboutUiAction

    data object LogoutClicked : AboutUiAction

    // Logout confirmation dialog positive button.
    data object LogoutConfirmed : AboutUiAction

    // Logout confirmation dialog cancelled by the user (cancel button, back or a touch outside).
    data object LogoutConfirmationDismissed : AboutUiAction

    // Sync & search preference click; counts towards the hidden troubleshooting entry.
    data object TroubleshootingClicked : AboutUiAction

    data class PasswordUnlocked(
        val action: AboutGatedAction,
    ) : AboutUiAction
}

internal sealed interface AboutUiEffect {
    data class ShowPasswordDialog(
        val password: String,
        val action: AboutGatedAction,
    ) : AboutUiEffect

    data object NavigateToLogoutSync : AboutUiEffect

    data object NavigateToTroubleshooting : AboutUiEffect
}
