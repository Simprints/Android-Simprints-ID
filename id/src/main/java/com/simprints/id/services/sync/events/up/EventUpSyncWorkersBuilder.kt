package com.simprints.id.services.sync.events.up

import androidx.work.OneTimeWorkRequest

interface SubjectsUpSyncWorkersBuilder {

    fun buildUpSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest>
}
