package com.simprints.infra.eventsync.sync.common

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.Companion.tagForType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class SyncWorkersLiveDataProviderImpl @Inject constructor(@ApplicationContext ctx: Context) :
    SyncWorkersLiveDataProvider {

    private val wm = WorkManager.getInstance(ctx)

    override fun getStartSyncReportersLiveData(): LiveData<List<WorkInfo>> =
        wm.getWorkInfosByTagLiveData((tagForType(EventSyncWorkerType.START_SYNC_REPORTER)))

    override fun getSyncWorkersLiveData(uniqueSyncId: String): LiveData<List<WorkInfo>> =
        wm.getWorkInfosByTagLiveData(getUniqueSyncIdTag(uniqueSyncId))
}
