package com.simprints.id.activities.dashboard.cards.daily_activity.repository

import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.activities.dashboard.cards.daily_activity.data.DailyActivityLocalDataSource
import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponseType
import javax.inject.Inject

class DashboardDailyActivityRepositoryImpl @Inject constructor(
    private val localDataSource: DailyActivityLocalDataSource,
    private val timeHelper: TimeHelper
) : DashboardDailyActivityRepository {

    override suspend fun getDailyActivity(): DashboardDailyActivityState {
        return clearOldActivityThenReturn {
            val enrolments = localDataSource.getEnrolmentsMadeToday()
            val identifications = localDataSource.getIdentificationsMadeToday()
            val verifications = localDataSource.getVerificationsMadeToday()

            DashboardDailyActivityState(
                enrolments,
                identifications,
                verifications
            )
        }
    }

    override suspend fun updateDailyActivity(appResponse: AppResponse) {
        when (appResponse.type) {
            AppResponseType.ENROL -> computeNewEnrolment()
            AppResponseType.IDENTIFY -> computeNewIdentification()
            AppResponseType.VERIFY -> computeNewVerification()
            AppResponseType.REFUSAL,
            AppResponseType.CONFIRMATION,
            AppResponseType.ERROR -> {
                //Other cases are ignore and we don't show info in dashboard for it
            }
        }
    }

    private suspend fun computeNewEnrolment() = clearOldActivityThenReturn {
        localDataSource.computeNewEnrolmentAndGet()

        localDataSource.setLastActivityTime(timeHelper.now())
    }

    private suspend fun computeNewIdentification() = clearOldActivityThenReturn {
        localDataSource.computeNewIdentificationAndGet()

        localDataSource.setLastActivityTime(timeHelper.now())
    }

    private suspend fun computeNewVerification() = clearOldActivityThenReturn {
        localDataSource.computeNewVerificationAndGet()

        localDataSource.setLastActivityTime(timeHelper.now())
    }

    private suspend fun <T> clearOldActivityThenReturn(block: suspend () -> T): T {
        val today = timeHelper.todayInMillis()
        val tomorrow = timeHelper.tomorrowInMillis()
        val lastActivityTime = localDataSource.getLastActivityTime()
        val lastActivityWasNotToday = lastActivityTime < today || lastActivityTime >= tomorrow

        if (lastActivityWasNotToday)
            localDataSource.clearActivity()

        return block.invoke()
    }

}
