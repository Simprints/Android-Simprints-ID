package com.simprints.id.activities.dashboard.cards.daily_activity.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.simprints.id.activities.dashboard.cards.daily_activity.data.DailyActivityLocalDataSource
import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponseType

class DashboardDailyActivityRepositoryImpl(
    private val localDataSource: DailyActivityLocalDataSource
) : DashboardDailyActivityRepository {

    private val dailyActivityState = DashboardDailyActivityState()
    private val liveData = MutableLiveData<DashboardDailyActivityState>()

    override fun getDailyActivity(): LiveData<DashboardDailyActivityState> {
        with(dailyActivityState) {
            enrolments = localDataSource.getEnrolmentsMadeToday()
            identifications = localDataSource.getIdentificationsMadeToday()
            verifications = localDataSource.getVerificationsMadeToday()
        }

        return liveData.apply {
            value = dailyActivityState
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

    private fun computeNewEnrolment() {
        dailyActivityState.enrolments = localDataSource.computeNewEnrolmentAndGet()
        liveData.value = dailyActivityState
    }

    private fun computeNewIdentification() {
        dailyActivityState.identifications = localDataSource.computeNewIdentificationAndGet()
        liveData.value = dailyActivityState
    }

    private fun computeNewVerification() {
        dailyActivityState.verifications = localDataSource.computeNewVerificationAndGet()
        liveData.value = dailyActivityState
    }

}
