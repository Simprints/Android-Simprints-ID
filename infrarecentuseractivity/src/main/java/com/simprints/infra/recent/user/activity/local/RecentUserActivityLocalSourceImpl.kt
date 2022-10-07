package com.simprints.infra.recent.user.activity.local

import androidx.datastore.core.DataStore
import com.simprints.infra.recent.user.activity.ProtoRecentUserActivity
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import kotlinx.coroutines.flow.first
import javax.inject.Inject

internal class RecentUserActivityLocalSourceImpl @Inject constructor(
    private val projectDataStore: DataStore<ProtoRecentUserActivity>,
) : RecentUserActivityLocalSource {

    override suspend fun getRecentUserActivity(): RecentUserActivity =
        projectDataStore.data.first().toDomain()

    override suspend fun updateRecentUserActivity(update: suspend (t: RecentUserActivity) -> RecentUserActivity): RecentUserActivity =
        projectDataStore
            .updateData { update(it.toDomain()).toProto() }
            .toDomain()

    override suspend fun clearRecentActivity() {
        projectDataStore.updateData { it.toBuilder().clear().build() }
    }
}
