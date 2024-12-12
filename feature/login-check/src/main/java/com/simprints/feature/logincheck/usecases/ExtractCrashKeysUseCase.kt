package com.simprints.feature.logincheck.usecases

import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.LoggingConstants.CrashReportingCustomKeys
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import javax.inject.Inject

internal class ExtractCrashKeysUseCase @Inject constructor(
    private val configManager: ConfigManager,
    private val authStore: AuthStore,
) {
    suspend operator fun invoke(action: ActionRequest) {
        val projectConfiguration = configManager.getProjectConfiguration()
        val deviceConfiguration = configManager.getDeviceConfiguration()
        Simber.tag(CrashReportingCustomKeys.PROJECT_ID, true).i(authStore.signedInProjectId)
        Simber.tag(CrashReportingCustomKeys.USER_ID, true).i(action.userId.toString())
        Simber.tag(CrashReportingCustomKeys.MODULE_IDS, true).i(deviceConfiguration.selectedModules.toString())
        Simber.tag(CrashReportingCustomKeys.SUBJECTS_DOWN_SYNC_TRIGGERS, true).i(projectConfiguration.synchronization.frequency.toString())
    }
}
