package com.simprints.id.activities.dashboard.cards.daily_activity.data

import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.tools.TimeHelper

class DailyActivityLocalDataSourceImpl(
    private val preferencesManager: RecentEventsPreferencesManager,
    private val timeHelper: TimeHelper
) : DailyActivityLocalDataSource {

    override fun getEnrolmentsMadeToday(): Int = clearOldActivityThenReturn {
        preferencesManager.enrolmentsToday
    }

    override fun getIdentificationsMadeToday(): Int = clearOldActivityThenReturn {
        preferencesManager.identificationsToday
    }

    override fun getVerificationsMadeToday(): Int = clearOldActivityThenReturn {
        preferencesManager.verificationsToday
    }

    override fun computeNewEnrolmentAndGet(): Int = clearOldActivityThenReturn {
        ++preferencesManager.enrolmentsToday
    }

    override fun computeNewIdentificationAndGet(): Int = clearOldActivityThenReturn {
        ++preferencesManager.identificationsToday
    }

    override fun computeNewVerificationAndGet(): Int = clearOldActivityThenReturn {
        ++preferencesManager.verificationsToday
    }

    private fun <T> clearOldActivityThenReturn(block: () -> T): T {
        val today = timeHelper.todayInMillis()
        val lastActivityTime = preferencesManager.lastActivityTime
        val lastActivityWasNotToday = lastActivityTime < today

        if (lastActivityWasNotToday)
            clearOldActivity()

        return block()
    }

    private fun clearOldActivity() {
        with(preferencesManager) {
            enrolmentsToday = 0
            identificationsToday = 0
            verificationsToday = 0
        }
    }

}
