package com.simprints.feature.orchestrator.usecases

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.orchestration.data.responses.AppEnrolResponse
import com.simprints.infra.orchestration.data.responses.AppIdentifyResponse
import com.simprints.infra.orchestration.data.responses.AppResponse
import com.simprints.infra.orchestration.data.responses.AppVerifyResponse
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import javax.inject.Inject

internal class UpdateDailyActivityUseCase @Inject constructor(
    private val recentUserActivityManager: RecentUserActivityManager,
    private val timeHelper: TimeHelper,
) {
    suspend operator fun invoke(appResponse: AppResponse) {
        when (appResponse) {
            is AppEnrolResponse -> recentUserActivityManager.updateRecentUserActivity { activity ->
                activity.also {
                    it.enrolmentsToday++
                    it.lastActivityTime = timeHelper.now().ms
                }
            }

            is AppIdentifyResponse -> recentUserActivityManager.updateRecentUserActivity { activity ->
                activity.also {
                    it.identificationsToday++
                    it.lastActivityTime = timeHelper.now().ms
                }
            }

            is AppVerifyResponse -> recentUserActivityManager.updateRecentUserActivity { activity ->
                activity.also {
                    it.verificationsToday++
                    it.lastActivityTime = timeHelper.now().ms
                }
            }

            else -> {
                // Other cases are ignore and we don't show info in dashboard for it
            }
        }
    }
}
