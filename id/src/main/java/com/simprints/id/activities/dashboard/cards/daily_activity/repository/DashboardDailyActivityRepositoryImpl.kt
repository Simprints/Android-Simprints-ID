package com.simprints.id.activities.dashboard.cards.daily_activity.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponseType

class DashboardDailyActivityRepositoryImpl(
    private val preferencesManager: PreferencesManager
) : DashboardDailyActivityRepository {

    private val dailyActivityState = DashboardDailyActivityState()
    private val liveData = MutableLiveData<DashboardDailyActivityState>()

    override fun getDailyActivity(): LiveData<DashboardDailyActivityState> {
        with(dailyActivityState) {
            enrolments = preferencesManager.enrolmentsToday
            identifications = preferencesManager.identificationsToday
            verifications = preferencesManager.verificationsToday
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

    override fun resetDailyActivity() {
        with(preferencesManager) {
            enrolmentsToday = 0
            identificationsToday = 0
            verificationsToday = 0
        }
    }

    private fun computeNewEnrolment() {
        dailyActivityState.enrolments = ++preferencesManager.enrolmentsToday
        liveData.value = dailyActivityState
    }

    private fun computeNewIdentification() {
        dailyActivityState.identifications = ++preferencesManager.identificationsToday
        liveData.value = dailyActivityState
    }

    private fun computeNewVerification() {
        dailyActivityState.verifications = ++preferencesManager.verificationsToday
        liveData.value = dailyActivityState
    }

}
