package com.simprints.id.services.scheduledSync.people.master.internal

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.simprints.id.services.scheduledSync.people.common.getUniqueSyncIdTag
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker

class SyncWorkersLiveDataProviderImpl(val ctx: Context) : SyncWorkersLiveDataProvider {

    private val wm: WorkManager
        get() = WorkManager.getInstance(ctx)


    override fun getMasterWorkersLiveData(): LiveData<List<WorkInfo>> =
        wm.getWorkInfosByTagLiveData(PeopleSyncMasterWorker.MASTER_SYNC_SCHEDULERS)

    override fun getSyncWorkersLiveData(uniqueSyncId: String): LiveData<List<WorkInfo>> =
        wm.getWorkInfosByTagLiveData(getUniqueSyncIdTag(uniqueSyncId))
}
