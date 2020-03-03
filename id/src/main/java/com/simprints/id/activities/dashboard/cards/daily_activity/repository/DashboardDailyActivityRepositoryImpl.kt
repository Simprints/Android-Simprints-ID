package com.simprints.id.activities.dashboard.cards.daily_activity.repository

import com.simprints.id.activities.dashboard.cards.daily_activity.data.DailyActivityLocalDataSource
import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponseType
import com.simprints.id.tools.TimeHelper

class DashboardDailyActivityRepositoryImpl(
    private val localDataSource: DailyActivityLocalDataSource,
    private val timeHelper: TimeHelper
) : DashboardDailyActivityRepository {

    private var dailyActivityState = DashboardDailyActivityState()

    override fun getDailyActivity(): DashboardDailyActivityState {
        return clearOldActivityThenReturn {
            val enrolments = localDataSource.getEnrolmentsMadeToday()
            val identifications = localDataSource.getIdentificationsMadeToday()
            val verifications = localDataSource.getVerificationsMadeToday()

            dailyActivityState = DashboardDailyActivityState(
                enrolments,
                identifications,
                verifications
            )

            dailyActivityState
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
        val enrolments = localDataSource.computeNewEnrolmentAndGet()

        dailyActivityState = DashboardDailyActivityState(
            enrolments,
            dailyActivityState.identifications,
            dailyActivityState.verifications
        )

        localDataSource.setLastActivityTime(timeHelper.now())
    }

    private fun computeNewIdentification() = clearOldActivityThenReturn {
        val identifications = localDataSource.computeNewIdentificationAndGet()

        dailyActivityState = DashboardDailyActivityState(
            dailyActivityState.enrolments,
            identifications,
            dailyActivityState.verifications
        )

        localDataSource.setLastActivityTime(timeHelper.now())
    }

    private fun computeNewVerification() = clearOldActivityThenReturn {
        val verifications = localDataSource.computeNewVerificationAndGet()

        dailyActivityState = DashboardDailyActivityState(
            dailyActivityState.enrolments,
            dailyActivityState.identifications,
            verifications
        )

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
