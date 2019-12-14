package com.simprints.id.services.scheduledSync.people.master

import androidx.lifecycle.LiveData

interface PeopleSyncStateProcessor {
    fun getLastSyncState(): LiveData<PeopleSyncState>
}
