package com.simprints.id.activities.dashboard.cards.daily_activity.repository

import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponseType

class DashboardDailyActivityRepositoryImpl(
    private val preferencesManager: PreferencesManager
) : DashboardDailyActivityRepository {

    override fun getDailyActivity(): DashboardDailyActivityState {
        val enrolments = preferencesManager.enrolmentsToday
        val identifications = preferencesManager.identificationsToday
        val verifications = preferencesManager.verificationsToday

        return DashboardDailyActivityState(enrolments, identifications, verifications)
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
        preferencesManager.enrolmentsToday++
    }

    private fun computeNewIdentification() {
        preferencesManager.identificationsToday++
    }

    private fun computeNewVerification() {
        preferencesManager.verificationsToday++
    }

}
