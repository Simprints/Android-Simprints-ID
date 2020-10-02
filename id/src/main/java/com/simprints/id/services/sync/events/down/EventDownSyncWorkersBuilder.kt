package com.simprints.id.services.sync.events.down

import androidx.work.OneTimeWorkRequest

interface EventDownSyncWorkersBuilder {

    suspend fun buildDownSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest>

}
