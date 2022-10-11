package com.simprints.id.activities.dashboard.cards.daily_activity.data

import com.simprints.infra.recent.user.activity.RecentUserActivityManager

class DailyActivityLocalDataSourceImpl(
    private val recentUserActivityManager: RecentUserActivityManager
) : DailyActivityLocalDataSource {

    override suspend fun getEnrolmentsMadeToday(): Int =
        recentUserActivityManager.getRecentUserActivity().enrolmentsToday

    override suspend fun getIdentificationsMadeToday(): Int =
        recentUserActivityManager.getRecentUserActivity().identificationsToday

    override suspend fun getVerificationsMadeToday(): Int =
        recentUserActivityManager.getRecentUserActivity().verificationsToday

    override suspend fun getLastActivityTime(): Long =
        recentUserActivityManager.getRecentUserActivity().lastActivityTime

    override suspend fun computeNewEnrolmentAndGet(): Int =
        recentUserActivityManager.updateRecentUserActivity {
            it.apply {
                it.enrolmentsToday++
            }
        }.enrolmentsToday

    override suspend fun computeNewIdentificationAndGet(): Int =
        recentUserActivityManager.updateRecentUserActivity {
            it.apply {
                it.identificationsToday++
            }
        }.identificationsToday

    override suspend fun computeNewVerificationAndGet(): Int =
        recentUserActivityManager.updateRecentUserActivity {
            it.apply {
                it.verificationsToday++
            }
        }.verificationsToday

    override suspend fun setLastActivityTime(lastActivityTime: Long) {
        recentUserActivityManager.updateRecentUserActivity {
            it.apply {
                it.lastActivityTime = lastActivityTime
            }
        }
    }

    override suspend fun clearActivity() = recentUserActivityManager.clearRecentActivity()

}
