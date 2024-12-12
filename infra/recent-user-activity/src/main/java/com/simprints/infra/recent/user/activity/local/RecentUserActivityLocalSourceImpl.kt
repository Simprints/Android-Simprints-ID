package com.simprints.infra.recent.user.activity.local

import androidx.datastore.core.DataStore
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.recent.user.activity.ProtoRecentUserActivity
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import javax.inject.Inject

internal class RecentUserActivityLocalSourceImpl @Inject constructor(
    private val recentUserActivityDataStore: DataStore<ProtoRecentUserActivity>,
    private val timeHelper: TimeHelper,
) : RecentUserActivityLocalSource {
    override suspend fun getRecentUserActivity(): RecentUserActivity =
        recentUserActivityDataStore.updateData { it.clearOldActivity() }.toDomain()

    override suspend fun updateRecentUserActivity(update: suspend (t: RecentUserActivity) -> RecentUserActivity): RecentUserActivity =
        recentUserActivityDataStore
            .updateData { update(it.clearOldActivity().toDomain()).toProto() }
            .toDomain()

    override suspend fun clearRecentActivity() {
        recentUserActivityDataStore.updateData { it.toBuilder().clear().build() }
    }

    private fun ProtoRecentUserActivity.clearOldActivity(): ProtoRecentUserActivity {
        val today = timeHelper.todayInMillis()
        val tomorrow = timeHelper.tomorrowInMillis()
        val lastActivityWasNotToday = lastActivityTime < today || lastActivityTime >= tomorrow
        if (lastActivityWasNotToday) {
            return toBuilder()
                .setEnrolmentsToday(0)
                .setIdentificationsToday(0)
                .setVerificationsToday(0)
                .build()
        }
        return this
    }
}
