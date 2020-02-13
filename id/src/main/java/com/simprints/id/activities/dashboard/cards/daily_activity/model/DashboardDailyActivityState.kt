package com.simprints.id.activities.dashboard.cards.daily_activity.model

data class DashboardDailyActivityState(
    var enrolments: Int = 0,
    var identifications: Int = 0,
    var verifications: Int = 0
) {

    fun hasNoActivity() = (enrolments == 0) && (identifications == 0) && (verifications == 0)

}
