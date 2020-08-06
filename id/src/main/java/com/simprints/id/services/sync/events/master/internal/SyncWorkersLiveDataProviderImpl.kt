package com.simprints.id.services.sync.events.master.internal

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.simprints.id.services.sync.events.common.getUniqueSyncIdTag
import com.simprints.id.services.sync.events.master.models.SubjectsSyncWorkerType
import com.simprints.id.services.sync.events.master.models.SubjectsSyncWorkerType.Companion.tagForType

class SyncWorkersLiveDataProviderImpl(val ctx: Context) : SyncWorkersLiveDataProvider {

    private val wm = WorkManager.getInstance(ctx)


    override fun getStartSyncReportersLiveData(): LiveData<List<WorkInfo>> =
        wm.getWorkInfosByTagLiveData((tagForType(SubjectsSyncWorkerType.START_SYNC_REPORTER)))

    override fun getSyncWorkersLiveData(uniqueSyncId: String): LiveData<List<WorkInfo>> =
        wm.getWorkInfosByTagLiveData(getUniqueSyncIdTag(uniqueSyncId))
}
