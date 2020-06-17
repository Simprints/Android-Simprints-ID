package com.simprints.id.services.scheduledSync.subjects.master.internal

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo

interface SyncWorkersLiveDataProvider {

    fun getStartSyncReportersLiveData(): LiveData<List<WorkInfo>>
    fun getSyncWorkersLiveData(uniqueSyncId: String): LiveData<List<WorkInfo>>
}
