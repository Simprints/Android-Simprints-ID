package com.simprints.id.services.scheduledSync.people.master

import androidx.lifecycle.LiveData
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncState

interface PeopleSyncStateProcessor {
    fun getLastSyncState(): LiveData<PeopleSyncState>
}
