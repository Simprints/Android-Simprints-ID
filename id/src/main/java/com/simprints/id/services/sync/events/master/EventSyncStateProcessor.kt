package com.simprints.id.services.sync.events.master

import androidx.lifecycle.LiveData
import com.simprints.id.services.sync.events.master.models.SubjectsSyncState

interface EventSyncStateProcessor {
    fun getLastSyncState(): LiveData<SubjectsSyncState>
}
