package com.simprints.feature.clientapi.session

import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import javax.inject.Inject

internal class ReportActionRequestEventsUseCase @Inject constructor(
    private val clientSessionManager: ClientSessionManager,
    private val recentUserActivityManager: RecentUserActivityManager,
) {

    suspend operator fun invoke(actionRequest: ActionRequest) {
        clientSessionManager.reportUnknownExtras(actionRequest.unknownExtras)
        if (actionRequest is ActionRequest.FlowAction) {
            clientSessionManager.reportConnectivityState()
        }
        clientSessionManager.reportRequestActionEvent(actionRequest)

        recentUserActivityManager.updateRecentUserActivity { recentActivity ->
            recentActivity.apply { lastUserUsed = actionRequest.userId }
        }
    }
}
