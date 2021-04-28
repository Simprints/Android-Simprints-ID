package com.simprints.id.activities.dashboard.cards.daily_activity.repository

import com.simprints.id.activities.dashboard.cards.daily_activity.data.DailyActivityLocalDataSource
import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponseType
import com.simprints.id.tools.time.TimeHelper

class DashboardDailyActivityRepositoryImpl(
    private val localDataSource: DailyActivityLocalDataSource,
    private val timeHelper: TimeHelper
) : DashboardDailyActivityRepository {

    override fun getDailyActivity(): DashboardDailyActivityState {
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

    @Suppress("NON_EXHAUSTIVE_WHEN")
    override fun updateDailyActivity(appResponse: AppResponse) {
        when (appResponse.type) {
            AppResponseType.ENROL -> computeNewEnrolment()
            AppResponseType.IDENTIFY -> computeNewIdentification()
            AppResponseType.VERIFY -> computeNewVerification()
        }
    }

    private fun computeNewEnrolment() = clearOldActivityThenReturn {
        localDataSource.computeNewEnrolmentAndGet()

        localDataSource.setLastActivityTime(timeHelper.now())
    }

    private fun computeNewIdentification() = clearOldActivityThenReturn {
        localDataSource.computeNewIdentificationAndGet()

        localDataSource.setLastActivityTime(timeHelper.now())
    }

    private fun computeNewVerification() = clearOldActivityThenReturn {
        localDataSource.computeNewVerificationAndGet()

        localDataSource.setLastActivityTime(timeHelper.now())
    }

    private fun <T> clearOldActivityThenReturn(block: () -> T): T {
        val today = timeHelper.todayInMillis()
        val tomorrow = timeHelper.tomorrowInMillis()
        val lastActivityTime = localDataSource.getLastActivityTime()
        val lastActivityWasNotToday = lastActivityTime < today || lastActivityTime >= tomorrow

        if (lastActivityWasNotToday)
            localDataSource.clearActivity()

        return block.invoke()
    }

}
