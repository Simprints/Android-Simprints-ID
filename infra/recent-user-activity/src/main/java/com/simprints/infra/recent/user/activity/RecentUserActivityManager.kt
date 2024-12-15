package com.simprints.infra.recent.user.activity

import com.simprints.infra.recent.user.activity.domain.RecentUserActivity

interface RecentUserActivityManager {
    /**
     * Returns the latest user activity. Before returning it, it will reset the count of enrolment,
     * identification and verification if the last activity is not today.
     */
    suspend fun getRecentUserActivity(): RecentUserActivity

    /**
     * Updates and returns the latest user activity. Before updating it, it will reset the count of enrolment,
     * identification and verification if the last activity is not today.
     */
    suspend fun updateRecentUserActivity(update: suspend (t: RecentUserActivity) -> RecentUserActivity): RecentUserActivity

    suspend fun clearRecentActivity()
}
