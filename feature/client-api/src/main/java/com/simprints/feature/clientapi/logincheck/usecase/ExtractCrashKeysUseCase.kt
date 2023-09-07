package com.simprints.feature.clientapi.logincheck.usecase

import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.logging.LoggingConstants.CrashReportingCustomKeys
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class ExtractCrashKeysUseCase @Inject constructor(
    private val configManager: ConfigManager,
    private val authStore: AuthStore,
) {

    suspend operator fun invoke(action: ActionRequest) {
        val projectConfiguration = configManager.getProjectConfiguration()
        val deviceConfiguration = configManager.getDeviceConfiguration()
        Simber.tag(CrashReportingCustomKeys.PROJECT_ID, true).i(authStore.signedInProjectId)
        Simber.tag(CrashReportingCustomKeys.USER_ID, true).i(action.userId)
        Simber.tag(CrashReportingCustomKeys.MODULE_IDS, true).i(deviceConfiguration.selectedModules.toString())
        Simber.tag(CrashReportingCustomKeys.SUBJECTS_DOWN_SYNC_TRIGGERS, true).i(projectConfiguration.synchronization.frequency.toString())
    }
}
