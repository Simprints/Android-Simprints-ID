package com.simprints.infra.eventsync.sync.common

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.Companion.tagForType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class SyncWorkersLiveDataProvider @Inject constructor(
    @ApplicationContext ctx: Context,
) {
    private val wm = WorkManager.getInstance(ctx)

    fun getStartSyncReportersLiveData(): LiveData<List<WorkInfo>> =
        wm.getWorkInfosByTagLiveData((tagForType(EventSyncWorkerType.START_SYNC_REPORTER)))

    fun getSyncWorkersLiveData(uniqueSyncId: String): LiveData<List<WorkInfo>> =
        wm.getWorkInfosByTagLiveData(getUniqueSyncIdTag(uniqueSyncId))
}
