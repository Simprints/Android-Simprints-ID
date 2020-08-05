package com.simprints.id.services.sync.subjects.master

import androidx.lifecycle.LiveData
import com.simprints.id.services.sync.subjects.master.models.SubjectsSyncState

interface SubjectsSyncStateProcessor {
    fun getLastSyncState(): LiveData<SubjectsSyncState>
}
