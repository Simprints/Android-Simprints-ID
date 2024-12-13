package com.simprints.infra.recent.user.activity

import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.infra.recent.user.activity.local.RecentUserActivityLocalSource
import javax.inject.Inject

internal class RecentUserActivityManagerImpl @Inject constructor(
    private val localSource: RecentUserActivityLocalSource,
) : RecentUserActivityManager {
    override suspend fun getRecentUserActivity(): RecentUserActivity = localSource.getRecentUserActivity()

    override suspend fun updateRecentUserActivity(update: suspend (t: RecentUserActivity) -> RecentUserActivity): RecentUserActivity =
        localSource.updateRecentUserActivity(update)

    override suspend fun clearRecentActivity() = localSource.clearRecentActivity()
}
