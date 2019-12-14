package com.simprints.id.services.scheduledSync.people.down.controllers

import androidx.work.WorkRequest
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_PEOPLE_SYNC_ALL_WORKERS

interface PeopleDownSyncWorkersBuilder {

    companion object {

        const val TAG_DOWN_MASTER_SYNC_ID = "TAG_DOWN_MASTER_SYNC_ID_"

        const val TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS = "DOWN_${TAG_PEOPLE_SYNC_ALL_WORKERS}"
        const val TAG_PEOPLE_DOWN_SYNC_ALL_DOWNLOADERS = "TAG_PEOPLE_DOWN_SYNC_ALL_DOWNLOADERS"
        const val TAG_PEOPLE_DOWN_SYNC_ALL_COUNTERS = "TAG_PEOPLE_DOWN_SYNC_ALL_COUNTERS"

    }

    suspend fun buildDownSyncWorkerChain(uniqueSyncId: String?): List<WorkRequest>

}
