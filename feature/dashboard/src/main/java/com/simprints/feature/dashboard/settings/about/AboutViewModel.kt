package com.simprints.feature.dashboard.settings.about

import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.common.Modality
import com.simprints.feature.troubleshooting.AutoResettingClickCounter
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.devicestate.DeviceStateDataTracker
import com.simprints.infra.uibase.viewmodel.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

@HiltViewModel
internal class AboutViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val deviceStateDataTracker: DeviceStateDataTracker,
    private val syncOrchestrator: SyncOrchestrator,
    private val recentUserActivityManager: RecentUserActivityManager,
) : BaseMviViewModel<AboutUiState, AboutUiAction, AboutUiEffect>(AboutUiState()) {
    private val troubleshootingClickCounter = AutoResettingClickCounter()

    override suspend fun handleAction(action: AboutUiAction) {
        when (action) {
            AboutUiAction.Load -> load()
            AboutUiAction.LogoutClicked -> requestLogoutConfirmation()
            AboutUiAction.LogoutConfirmed -> confirmLogout()
            AboutUiAction.LogoutConfirmationDismissed -> reduce { it.copy(isLogoutConfirmationVisible = false) }
            AboutUiAction.TroubleshootingClicked -> countTroubleshootingClick()
            is AboutUiAction.PasswordUnlocked -> unlock(action.action)
        }
    }

    private suspend fun load() {
        val configuration = configRepository.getProjectConfiguration()
        val syncAndSearchConfig = SyncAndSearchConfig(
            configuration.synchronization.down.simprints
                ?.partitionType
                ?.name ?: "CommCare",
            configuration.identification.poolType.name,
        )
        val recentUserActivity = recentUserActivityManager.getRecentUserActivity()
        reduce {
            it.copy(
                syncAndSearchConfig = syncAndSearchConfig,
                isScannerVersionVisible = configuration.general.modalities.contains(Modality.FINGERPRINT),
                lastScannerVersion = recentUserActivity.lastScannerVersion,
                passwordConfig = configuration.general.settingsPassword,
            )
        }
    }

    private suspend fun requestLogoutConfirmation() {
        val password = uiState.value.passwordConfig.getNullablePassword()
        if (password != null) {
            emitEffect(AboutUiEffect.ShowPasswordDialog(password, AboutGatedAction.LOGOUT))
        } else {
            reduce { it.copy(isLogoutConfirmationVisible = true) }
        }
    }

    private suspend fun confirmLogout() {
        reduce { it.copy(isLogoutConfirmationVisible = false) }
        processLogoutRequest()
    }

    private suspend fun unlock(action: AboutGatedAction) {
        reduce { it.copy(passwordConfig = SettingsPasswordConfig.Unlocked) }
        when (action) {
            AboutGatedAction.LOGOUT -> processLogoutRequest()
            AboutGatedAction.TROUBLESHOOTING -> emitEffect(AboutUiEffect.NavigateToTroubleshooting)
        }
    }

    // Pending data is read freshly on every request. Without pending data the logout command is enough: navigation to request
    // login comes from the DashboardActivity logout broadcast, so no effect is sent.
    private suspend fun processLogoutRequest() {
        if (canSyncDataToSimprints() && (hasEventsToUpload() || hasSamplesToUpload())) {
            emitEffect(AboutUiEffect.NavigateToLogoutSync)
        } else {
            syncOrchestrator.execute(OneTime.Logout.start())
        }
    }

    private suspend fun hasEventsToUpload(): Boolean = try {
        deviceStateDataTracker.hasPendingEvents()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (t: Throwable) {
        Simber.i("Could not check pending events before logout", t, tag = SYNC)
        true
    }

    private suspend fun hasSamplesToUpload(): Boolean = deviceStateDataTracker.getCurrentDeviceDataState().pendingSamples != 0

    private suspend fun canSyncDataToSimprints(): Boolean = configRepository.getProjectConfiguration().canSyncDataToSimprints()

    private suspend fun countTroubleshootingClick() {
        if (troubleshootingClickCounter.handleClick(viewModelScope)) {
            val password = uiState.value.passwordConfig.getNullablePassword()
            if (password != null) {
                emitEffect(AboutUiEffect.ShowPasswordDialog(password, AboutGatedAction.TROUBLESHOOTING))
            } else {
                emitEffect(AboutUiEffect.NavigateToTroubleshooting)
            }
        }
    }

    init {
        onAction(AboutUiAction.Load)
    }
}
