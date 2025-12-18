package com.simprints.feature.logincheck.usecases

import com.simprints.core.SessionCoroutineScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.infra.events.event.domain.models.ConnectivitySnapshotEvent
import com.simprints.infra.events.event.domain.models.SuspiciousIntentEvent
import com.simprints.infra.events.event.domain.models.callout.BiometricDataSource
import com.simprints.infra.events.event.domain.models.callout.ConfirmationCalloutEventV3
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEventV3
import com.simprints.infra.events.event.domain.models.callout.EnrolmentLastBiometricsCalloutEventV3
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEventV3
import com.simprints.infra.events.event.domain.models.callout.VerificationCalloutEventV3
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class ReportActionRequestEventsUseCase @Inject constructor(
    private val sessionEventRepository: SessionEventRepository,
    private val timeHelper: TimeHelper,
    private val simNetworkUtils: SimNetworkUtils,
    private val recentUserActivityManager: RecentUserActivityManager,
    @param:SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) {
    suspend operator fun invoke(actionRequest: ActionRequest) {
        reportUnknownExtras(actionRequest)
        if (actionRequest is ActionRequest.FlowAction) {
            addConnectivityStateEvent()
        }
        addRequestActionEvent(actionRequest)

        recentUserActivityManager.updateRecentUserActivity { recentActivity ->
            recentActivity.apply { lastUserUsed = actionRequest.userId }
        }
    }

    private fun reportUnknownExtras(actionRequest: ActionRequest) {
        if (actionRequest.unknownExtras.isNotEmpty()) {
            sessionCoroutineScope.launch {
                sessionEventRepository.addOrUpdateEvent(SuspiciousIntentEvent(timeHelper.now(), actionRequest.unknownExtras))
            }
        }
    }

    private suspend fun addConnectivityStateEvent() {
        sessionEventRepository.addOrUpdateEvent(ConnectivitySnapshotEvent(timeHelper.now(), simNetworkUtils.connectionsStates))
    }

    private suspend fun addRequestActionEvent(request: ActionRequest) {
        val startTime = timeHelper.now()
        val event = with(request) {
            when (this) {
                is ActionRequest.EnrolActionRequest -> EnrolmentCalloutEventV3(
                    startTime,
                    projectId,
                    userId,
                    moduleId,
                    metadata,
                    BiometricDataSource.fromString(biometricDataSource),
                )
                is ActionRequest.IdentifyActionRequest -> IdentificationCalloutEventV3(
                    startTime,
                    projectId,
                    userId,
                    moduleId,
                    metadata,
                    BiometricDataSource.fromString(biometricDataSource),
                )
                is ActionRequest.VerifyActionRequest -> VerificationCalloutEventV3(
                    startTime,
                    projectId,
                    userId,
                    moduleId,
                    verifyGuid,
                    metadata,
                    BiometricDataSource.fromString(biometricDataSource),
                )
                is ActionRequest.ConfirmIdentityActionRequest -> ConfirmationCalloutEventV3(
                    startTime,
                    projectId,
                    selectedGuid,
                    sessionId,
                    metadata,
                )
                is ActionRequest.EnrolLastBiometricActionRequest -> EnrolmentLastBiometricsCalloutEventV3(
                    startTime,
                    projectId,
                    userId,
                    moduleId,
                    metadata,
                    sessionId,
                )
            }
        }
        sessionEventRepository.addOrUpdateEvent(event)
    }
}
