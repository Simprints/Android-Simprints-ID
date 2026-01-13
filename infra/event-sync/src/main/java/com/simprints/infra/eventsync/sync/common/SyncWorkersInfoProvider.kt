package com.simprints.infra.eventsync.sync.common

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.Companion.tagForType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncWorkersInfoProvider @Inject constructor(
    @ApplicationContext ctx: Context,
) {
    private val wm = WorkManager.getInstance(ctx)

    fun getStartSyncReporters(): Flow<List<WorkInfo>> = wm.getWorkInfosByTagFlow(tagForType(EventSyncWorkerType.START_SYNC_REPORTER))

    fun getSyncWorkerInfos(uniqueSyncId: String): Flow<List<WorkInfo>> = wm.getWorkInfosByTagFlow(getUniqueSyncIdTag(uniqueSyncId))
}
