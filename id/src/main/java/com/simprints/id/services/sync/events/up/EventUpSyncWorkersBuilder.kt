package com.simprints.id.services.sync.events.up

import androidx.work.OneTimeWorkRequest

interface EventUpSyncWorkersBuilder {

    suspend fun buildUpSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest>
}
