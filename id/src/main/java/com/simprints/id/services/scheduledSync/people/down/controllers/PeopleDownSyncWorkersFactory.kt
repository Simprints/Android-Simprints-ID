package com.simprints.id.services.scheduledSync.people.down.controllers

import androidx.work.WorkRequest
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker.Companion.TAG_PEOPLE_SYNC_ALL_WORKERS

interface PeopleDownSyncWorkersFactory {

    companion object {

        const val TAG_DOWN_MASTER_SYNC_ID = "TAG_DOWN_MASTER_SYNC_ID_"

        const val TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS = "DOWN_${TAG_PEOPLE_SYNC_ALL_WORKERS}"

    }

    suspend fun buildDownSyncWorkerChain(uniqueSyncId: String?): List<WorkRequest>

}
