package com.simprints.feature.orchestrator.usecases

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.moduleapi.app.responses.IAppResponse
import com.simprints.moduleapi.app.responses.IAppResponseType
import javax.inject.Inject

internal class UpdateDailyActivityUseCase @Inject constructor(
    private val recentUserActivityManager: RecentUserActivityManager,
    private val timeHelper: TimeHelper,
) {
    suspend operator fun invoke(appResponse: IAppResponse) {
        when (appResponse.type) {
            IAppResponseType.ENROL -> recentUserActivityManager.updateRecentUserActivity { activity ->
                activity.also {
                    it.enrolmentsToday++
                    it.lastActivityTime = timeHelper.now()
                }
            }

            IAppResponseType.IDENTIFY -> recentUserActivityManager.updateRecentUserActivity { activity ->
                activity.also {
                    it.identificationsToday++
                    it.lastActivityTime = timeHelper.now()
                }
            }

            IAppResponseType.VERIFY -> recentUserActivityManager.updateRecentUserActivity { activity ->
                activity.also {
                    it.verificationsToday++
                    it.lastActivityTime = timeHelper.now()
                }
            }

            else -> {
                //Other cases are ignore and we don't show info in dashboard for it
            }
        }
    }
}
