package com.simprints.id.services.scheduledSync.people.master.internal

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.simprints.id.services.scheduledSync.people.common.getUniqueSyncIdTag
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerType
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerType.Companion.tagForType

class SyncWorkersLiveDataProviderImpl(val ctx: Context) : SyncWorkersLiveDataProvider {

    private val wm = WorkManager.getInstance(ctx)


    override fun getStartSyncReportersLiveData(): LiveData<List<WorkInfo>> =
        wm.getWorkInfosByTagLiveData((tagForType(PeopleSyncWorkerType.START_SYNC_REPORTER)))

    override fun getSyncWorkersLiveData(uniqueSyncId: String): LiveData<List<WorkInfo>> =
        wm.getWorkInfosByTagLiveData(getUniqueSyncIdTag(uniqueSyncId))
}
