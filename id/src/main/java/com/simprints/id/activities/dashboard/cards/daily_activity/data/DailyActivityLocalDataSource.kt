package com.simprints.id.activities.dashboard.cards.daily_activity.data

interface DailyActivityLocalDataSource {
    fun getEnrolmentsMadeToday(): Int
    fun getIdentificationsMadeToday(): Int
    fun getVerificationsMadeToday(): Int
    fun computeNewEnrolmentAndGet(): Int
    fun computeNewIdentificationAndGet(): Int
    fun computeNewVerificationAndGet(): Int
}
