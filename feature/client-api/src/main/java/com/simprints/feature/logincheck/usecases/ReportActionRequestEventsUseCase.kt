package com.simprints.feature.logincheck.usecases

import com.simprints.feature.clientapi.usecases.SimpleEventReporter
import com.simprints.feature.orchestrator.models.ActionRequest
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import javax.inject.Inject

internal class ReportActionRequestEventsUseCase @Inject constructor(
    private val simpleEventReporter: SimpleEventReporter,
    private val recentUserActivityManager: RecentUserActivityManager,
) {

    suspend operator fun invoke(actionRequest: ActionRequest) {
        simpleEventReporter.addUnknownExtrasEvent(actionRequest.unknownExtras)
        if (actionRequest is ActionRequest.FlowAction) {
            simpleEventReporter.addConnectivityStateEvent()
        }
        simpleEventReporter.addRequestActionEvent(actionRequest)

        recentUserActivityManager.updateRecentUserActivity { recentActivity ->
            recentActivity.apply { lastUserUsed = actionRequest.userId }
        }
    }
}
