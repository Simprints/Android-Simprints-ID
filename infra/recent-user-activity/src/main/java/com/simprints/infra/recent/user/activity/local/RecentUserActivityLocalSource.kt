package com.simprints.infra.recent.user.activity.local

import com.simprints.infra.recent.user.activity.domain.RecentUserActivity

internal interface RecentUserActivityLocalSource {
    suspend fun getRecentUserActivity(): RecentUserActivity

    suspend fun updateRecentUserActivity(update: suspend (t: RecentUserActivity) -> RecentUserActivity): RecentUserActivity

    suspend fun clearRecentActivity()
}
