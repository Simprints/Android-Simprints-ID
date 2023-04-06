package com.simprints.infra.eventsync.sync.common

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo

internal interface SyncWorkersLiveDataProvider {

    fun getStartSyncReportersLiveData(): LiveData<List<WorkInfo>>
    fun getSyncWorkersLiveData(uniqueSyncId: String): LiveData<List<WorkInfo>>
}
