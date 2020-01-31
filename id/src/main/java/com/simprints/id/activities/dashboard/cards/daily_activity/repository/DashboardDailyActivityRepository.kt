package com.simprints.id.activities.dashboard.cards.daily_activity.repository

import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState
import com.simprints.id.data.prefs.PreferencesManager

class DashboardDailyActivityRepository(private val preferencesManager: PreferencesManager) {

    fun getDailyActivity(): DashboardDailyActivityState {
        val enrolments = preferencesManager.enrolmentsToday
        val identifications = preferencesManager.identificationsToday
        val verifications = preferencesManager.verificationsToday

        return DashboardDailyActivityState(enrolments, identifications, verifications)
    }

    fun computeNewEnrolment() {
        preferencesManager.enrolmentsToday++
    }

    fun computeNewIdentification() {
        preferencesManager.identificationsToday++
    }

    fun computeNewVerification() {
        preferencesManager.verificationsToday++
    }

    fun resetCounts() {
        with(preferencesManager) {
            enrolmentsToday = 0
            identificationsToday = 0
            verificationsToday = 0
        }
    }

}
