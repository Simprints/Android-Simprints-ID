package com.simprints.feature.logincheck.usecases

import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.logging.LoggingConstants.CrashReportingCustomKeys
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import javax.inject.Inject

internal class ExtractCrashKeysUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val authStore: AuthStore,
) {
    suspend operator fun invoke(action: ActionRequest) {
        val projectConfiguration = configRepository.getProjectConfiguration()
        val deviceConfiguration = configRepository.getDeviceConfiguration()
        Simber.setUserProperty(CrashReportingCustomKeys.PROJECT_ID, authStore.signedInProjectId)
        Simber.setUserProperty(CrashReportingCustomKeys.USER_ID, action.userId.toString())
        Simber.setUserProperty(CrashReportingCustomKeys.MODULE_IDS, deviceConfiguration.selectedModules.toString())
        Simber.setUserProperty(
            CrashReportingCustomKeys.SUBJECTS_DOWN_SYNC_TRIGGERS,
            projectConfiguration.synchronization.down.simprints?.frequency?.toString() ?: "CommCare periodic sync",
        )
    }
}
