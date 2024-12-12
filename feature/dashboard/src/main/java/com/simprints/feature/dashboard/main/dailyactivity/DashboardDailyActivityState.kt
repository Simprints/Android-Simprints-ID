package com.simprints.feature.dashboard.main.dailyactivity

internal data class DashboardDailyActivityState(
    val enrolments: Int,
    val identifications: Int,
    val verifications: Int,
) {
    fun hasNoActivity() = !hasEnrolments() && !hasIdentifications() && !hasVerifications()

    fun hasEnrolments() = enrolments > 0

    fun hasIdentifications() = identifications > 0

    fun hasVerifications() = verifications > 0
}
