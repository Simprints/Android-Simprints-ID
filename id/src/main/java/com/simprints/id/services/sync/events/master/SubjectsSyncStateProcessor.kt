package com.simprints.id.services.sync.events.master

import androidx.lifecycle.LiveData
import com.simprints.id.services.sync.events.master.models.SubjectsSyncState

interface SubjectsSyncStateProcessor {
    fun getLastSyncState(): LiveData<SubjectsSyncState>
}
