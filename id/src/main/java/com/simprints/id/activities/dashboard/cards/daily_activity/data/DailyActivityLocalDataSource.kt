package com.simprints.id.activities.dashboard.cards.daily_activity.data

interface DailyActivityLocalDataSource {
    suspend fun getEnrolmentsMadeToday(): Int
    suspend fun getIdentificationsMadeToday(): Int
    suspend fun getVerificationsMadeToday(): Int
    suspend fun getLastActivityTime(): Long
    suspend fun computeNewEnrolmentAndGet(): Int
    suspend fun computeNewIdentificationAndGet(): Int
    suspend fun computeNewVerificationAndGet(): Int
    suspend fun setLastActivityTime(lastActivityTime: Long)
    suspend fun clearActivity()
}
