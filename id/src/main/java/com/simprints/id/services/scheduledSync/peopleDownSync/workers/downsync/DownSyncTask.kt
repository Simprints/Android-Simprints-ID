package com.simprints.id.services.scheduledSync.peopleDownSync.workers.downsync

import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope

interface DownSyncTask {

    suspend fun execute(subSyncScope: SubSyncScope)
}
