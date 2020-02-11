package com.simprints.id.activities.dashboard.cards.daily_activity.data

import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager

class DailyActivityLocalDataSourceImpl(
    private val preferencesManager: RecentEventsPreferencesManager
) : DailyActivityLocalDataSource {

    override fun getEnrolmentsMadeToday(): Int = preferencesManager.enrolmentsToday

    override fun getIdentificationsMadeToday(): Int = preferencesManager.identificationsToday

    override fun getVerificationsMadeToday(): Int = preferencesManager.verificationsToday

    override fun getLastActivityTime(): Long = preferencesManager.lastActivityTime

    override fun computeNewEnrolmentAndGet(): Int = ++preferencesManager.enrolmentsToday

    override fun computeNewIdentificationAndGet(): Int = ++preferencesManager.identificationsToday

    override fun computeNewVerificationAndGet(): Int = ++preferencesManager.verificationsToday

    override fun setLastActivityTime(lastActivityTime: Long) {
        preferencesManager.lastActivityTime = lastActivityTime
    }

    override fun clearActivity() {
        with(preferencesManager) {
            enrolmentsToday = 0
            identificationsToday = 0
            verificationsToday = 0
        }
    }

}
