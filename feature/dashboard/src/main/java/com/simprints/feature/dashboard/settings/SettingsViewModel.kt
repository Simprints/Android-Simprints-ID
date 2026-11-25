package com.simprints.feature.dashboard.settings

import com.simprints.core.domain.common.Modality
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.ConfigSyncCache
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.events.device.DeviceEventTracker
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SETTINGS
import com.simprints.infra.logging.Simber
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.uibase.viewmodel.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val syncOrchestrator: SyncOrchestrator,
    private val configSyncCache: ConfigSyncCache,
    private val deviceEventTracker: DeviceEventTracker,
) : BaseMviViewModel<SettingsUiState, SettingsUiAction, SettingsUiEffect>(SettingsUiState()) {
    // Only touched from the action queue, so a plain field is enough.
    private var configRefreshJob: Job? = null

    override suspend fun handleAction(action: SettingsUiAction) {
        when (action) {
            SettingsUiAction.Load -> load()
            SettingsUiAction.ScreenStarted -> refreshConfigLastUpdated()
            SettingsUiAction.LanguageClicked -> requestLanguageSelection()
            is SettingsUiAction.PasswordUnlocked -> unlock(action.action)
            is SettingsUiAction.LanguageSelected -> updateLanguage(action.code)
            SettingsUiAction.LanguageDialogDismissed -> reduce { it.copy(isLanguageDialogVisible = false) }
            SettingsUiAction.UpdateConfigClicked -> scheduleConfigUpdate()
        }
    }

    private suspend fun load() {
        val configuration = configRepository.getProjectConfiguration()
        val configLastUpdated = configSyncCache.sinceLastUpdateTime()
        val languageCode = configRepository.getDeviceConfiguration().language
        reduce {
            it.copy(
                isFingerprintSettingsVisible = configuration.general.modalities.contains(Modality.FINGERPRINT),
                isFaceAutoCaptureVisible = configuration.face?.isAutoCapture == true,
                languageCode = languageCode,
                languageOptions = configuration.general.languageOptions,
                passwordConfig = configuration.general.settingsPassword,
                configLastUpdated = configLastUpdated,
            )
        }
    }

    // The cache only offers text relative to "now", so it is re-read whenever the screen starts.
    private suspend fun refreshConfigLastUpdated() {
        val configLastUpdated = configSyncCache.sinceLastUpdateTime()
        reduce { it.copy(configLastUpdated = configLastUpdated) }
    }

    private suspend fun requestLanguageSelection() {
        val state = uiState.value
        val password = state.passwordConfig.getNullablePassword()
        if (password != null) {
            emitEffect(SettingsUiEffect.ShowPasswordDialog(password, SettingsGatedAction.LANGUAGE))
        } else {
            showLanguageDialog()
        }
    }

    private suspend fun unlock(action: SettingsGatedAction) {
        reduce { it.copy(passwordConfig = SettingsPasswordConfig.Unlocked) }
        when (action) {
            SettingsGatedAction.LANGUAGE -> showLanguageDialog()
        }
    }

    private fun showLanguageDialog() {
        reduce { it.copy(isLanguageDialogVisible = true) }
    }

    private suspend fun updateLanguage(code: String) {
        // Closed before the save, so a second pick during the save cannot relaunch the app twice.
        reduce { it.copy(isLanguageDialogVisible = false) }
        configRepository.updateDeviceConfiguration { configuration ->
            configuration
                .apply { this.language = code }
                .also {
                    deviceEventTracker.trackDeviceConfigurationUpdatedEvent(
                        deviceConfiguration = it,
                        isLocalChange = true,
                    )
                }
        }
        reduce { it.copy(languageCode = code) }
        Simber.i("Language set to $code", tag = SETTINGS)
        emitEffect(SettingsUiEffect.RelaunchApp)
    }

    private fun scheduleConfigUpdate() {
        // The refresh flow never completes. Each click replaces the previous collector, so repeated clicks keep a single one
        // and one refresh emission sends one toast. Every emission is collected: the first one can come from WorkManager
        // before the new work is stored, and a later one carries the real completion.
        configRefreshJob?.cancel()
        reduce { it.copy(isConfigUpdateScheduled = true) }
        configRefreshJob = launchWork {
            syncOrchestrator.refreshConfiguration().collect {
                emitEffect(SettingsUiEffect.ConfigUpdated)
                val configLastUpdated = configSyncCache.sinceLastUpdateTime()
                reduce { it.copy(configLastUpdated = configLastUpdated, isConfigUpdateScheduled = false) }
            }
        }
    }

    init {
        onAction(SettingsUiAction.Load)
    }
}
