package com.simprints.id.services.sync.events.master.internal

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.simprints.id.services.sync.events.common.getUniqueSyncIdTag
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerType
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerType.Companion.tagForType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SyncWorkersLiveDataProviderImpl @Inject constructor(@ApplicationContext ctx: Context) :
    SyncWorkersLiveDataProvider {

    private val wm = WorkManager.getInstance(ctx)

    override fun getStartSyncReportersLiveData(): LiveData<List<WorkInfo>> =
        wm.getWorkInfosByTagLiveData((tagForType(EventSyncWorkerType.START_SYNC_REPORTER)))

    override fun getSyncWorkersLiveData(uniqueSyncId: String): LiveData<List<WorkInfo>> =
        wm.getWorkInfosByTagLiveData(getUniqueSyncIdTag(uniqueSyncId))
}
