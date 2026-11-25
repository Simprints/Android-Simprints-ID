package com.simprints.feature.dashboard.logout.syncdecline

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.uibase.viewmodel.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class LogoutSyncDeclineViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val syncOrchestrator: SyncOrchestrator,
) : BaseMviViewModel<LogoutSyncDeclineUiState, LogoutSyncDeclineUiAction, LogoutSyncDeclineUiEffect>(LogoutSyncDeclineUiState()) {
    override suspend fun handleAction(action: LogoutSyncDeclineUiAction) {
        when (action) {
            LogoutSyncDeclineUiAction.ConfirmClicked -> requestLogoutConfirmation()
            LogoutSyncDeclineUiAction.LogoutConfirmed -> logout()
            LogoutSyncDeclineUiAction.LogoutConfirmationDismissed -> reduce { it.copy(isLogoutConfirmationVisible = false) }
        }
    }

    // The password configuration is read on every click, never cached, so a changed configuration applies to the next click.
    private suspend fun requestLogoutConfirmation() {
        val password = configRepository.getProjectConfiguration().general.settingsPassword.getNullablePassword()
        if (password != null) {
            emitEffect(LogoutSyncDeclineUiEffect.ShowPasswordDialog(password))
        } else {
            reduce { it.copy(isLogoutConfirmationVisible = true) }
        }
    }

    // Navigation after logout is driven by the DashboardActivity logout broadcast, not by this screen.
    private fun logout() {
        reduce { it.copy(isLogoutConfirmationVisible = false) }
        syncOrchestrator.execute(OneTime.Logout.start())
    }
}
