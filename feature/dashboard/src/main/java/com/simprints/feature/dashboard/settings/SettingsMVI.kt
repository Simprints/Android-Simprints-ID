package com.simprints.feature.dashboard.settings

import com.simprints.infra.config.store.models.SettingsPasswordConfig

internal data class SettingsUiState(
    // Finger selection and audio alert. null: not loaded yet, both keep their XML defaults.
    val isFingerprintSettingsVisible: Boolean? = null,
    // null: not loaded yet, the preference keeps its XML default (hidden).
    val isFaceAutoCaptureVisible: Boolean? = null,
    // null: not loaded yet, the language summary keeps its XML default (none).
    val languageCode: String? = null,
    val languageOptions: List<String> = emptyList(),
    val passwordConfig: SettingsPasswordConfig = SettingsPasswordConfig.NotSet,
    // Relative "since last update" text from ConfigSyncCache, re-read on every ScreenStarted so it is current whenever the screen
    // is shown (KI-2 fix). null: not read yet, the update-config summary keeps its XML default.
    val configLastUpdated: String? = null,
    // From the update-config click until the next refresh emission.
    val isConfigUpdateScheduled: Boolean = false,
    // Shows languageOptions with languageCode selected. State, not an effect, so the dialog comes back after view re-creation.
    val isLanguageDialogVisible: Boolean = false,
) {
    val updateConfigSummary: UpdateConfigSummary
        get() = when {
            isConfigUpdateScheduled -> UpdateConfigSummary.Scheduled
            configLastUpdated != null -> UpdateConfigSummary.LastUpdated(configLastUpdated)
            else -> UpdateConfigSummary.Empty
        }

    sealed interface UpdateConfigSummary {
        data object Empty : UpdateConfigSummary

        data object Scheduled : UpdateConfigSummary

        data class LastUpdated(
            val sinceLastUpdate: String,
        ) : UpdateConfigSummary
    }
}

// Actions that sit behind the settings password. The key is passed through SettingsPasswordDialogFragment and back.
internal enum class SettingsGatedAction(
    val key: String,
) {
    LANGUAGE("language"),
    ;

    companion object {
        fun fromKey(key: String?): SettingsGatedAction? = entries.firstOrNull { it.key == key }
    }
}

internal sealed interface SettingsUiAction {
    // Sent once, from the ViewModel's init.
    data object Load : SettingsUiAction

    // Sent each time the screen's view reaches STARTED: re-reads the "since last update" text.
    data object ScreenStarted : SettingsUiAction

    data object LanguageClicked : SettingsUiAction

    data class PasswordUnlocked(
        val action: SettingsGatedAction,
    ) : SettingsUiAction

    data class LanguageSelected(
        val code: String,
    ) : SettingsUiAction

    data object LanguageDialogDismissed : SettingsUiAction

    data object UpdateConfigClicked : SettingsUiAction
}

internal sealed interface SettingsUiEffect {
    data class ShowPasswordDialog(
        val password: String,
        val action: SettingsGatedAction,
    ) : SettingsUiEffect

    // "Configuration updated" toast, once per update-config refresh.
    data object ConfigUpdated : SettingsUiEffect

    // Sent only after the new language is saved, so the relaunched app starts in it (KI-3 fix).
    data object RelaunchApp : SettingsUiEffect
}
