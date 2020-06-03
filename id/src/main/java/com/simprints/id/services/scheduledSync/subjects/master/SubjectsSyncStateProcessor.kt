package com.simprints.id.services.scheduledSync.subjects.master

import androidx.lifecycle.LiveData
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncState

interface SubjectsSyncStateProcessor {
    fun getLastSyncState(): LiveData<SubjectsSyncState>
}
