package com.simprints.infra.recent.user.activity

import com.simprints.infra.recent.user.activity.domain.RecentUserActivity

interface RecentUserActivityManager {

    suspend fun getRecentUserActivity(): RecentUserActivity

    suspend fun updateRecentUserActivity(update: suspend (t: RecentUserActivity) -> RecentUserActivity): RecentUserActivity

    suspend fun clearRecentActivity()
}
