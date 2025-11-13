package com.simprints.feature.orchestrator.usecases.steps

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class FallbackToCommCareDataSourceIfNeededUseCase @Inject constructor(
    private val eventSyncCache: EventSyncCache,
    private val timeHelper: TimeHelper,
) {
    suspend operator fun invoke(
        action: ActionRequest.EnrolActionRequest,
        projectConfiguration: ProjectConfiguration,
    ): ActionRequest.EnrolActionRequest =
        action.takeUnless { shouldFallbackToCommCareDataSource(it.biometricDataSource, projectConfiguration) }
            ?: action.copy(biometricDataSource = BiometricDataSource.COMMCARE)

    suspend operator fun invoke(
        action: ActionRequest.IdentifyActionRequest,
        projectConfiguration: ProjectConfiguration,
    ): ActionRequest.IdentifyActionRequest =
        action.takeUnless { shouldFallbackToCommCareDataSource(it.biometricDataSource, projectConfiguration) }
            ?: action.copy(biometricDataSource = BiometricDataSource.COMMCARE)

    private suspend fun shouldFallbackToCommCareDataSource(
        biometricDataSource: String,
        projectConfiguration: ProjectConfiguration,
    ): Boolean {
        // Only fallback if the current data source is not CommCare
        if (biometricDataSource == BiometricDataSource.COMMCARE) return false
        // Only fallback if CommCare sync is configured
        if (projectConfiguration.synchronization.down.commCare == null) return false

        val lastSyncTime = eventSyncCache.readLastSuccessfulSyncTime()
        val thresholdMs = TimeUnit.DAYS.toMillis(projectConfiguration.experimental().fallbackToCommCareThresholdDays)
        // If there has been a successful sync within the threshold, don't fallback
        if (lastSyncTime != null && timeHelper.msBetweenNowAndTime(lastSyncTime) < thresholdMs) {
            return false
        }

        Simber.w(
            message = "Falling back to CommCare data source because threshold is $thresholdMs and last sync was at $lastSyncTime",
            t = Exception("Fallback to CommCare just-in-time reading"),
            tag = SYNC,
        )
        return true
    }
}
