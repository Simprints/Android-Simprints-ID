package com.simprints.id.services.scheduledSync.people.master.internal

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo

interface SyncWorkersLiveDataProvider {
    fun getMasterWorkersLiveData(): LiveData<List<WorkInfo>>
    fun getSyncWorkersLiveData(uniqueSyncId: String): LiveData<List<WorkInfo>>
}
