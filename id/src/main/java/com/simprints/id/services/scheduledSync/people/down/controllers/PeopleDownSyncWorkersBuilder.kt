package com.simprints.id.services.scheduledSync.people.down.controllers

import androidx.work.WorkRequest
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_PEOPLE_SYNC_WORKERS

interface PeopleDownSyncWorkerBuilder {

    companion object {

        const val TAG_PEOPLE_DOWN_SYNC_WORKERS = "DOWN_${TAG_PEOPLE_SYNC_WORKERS}"
        const val TAG_PEOPLE_DOWN_SYNC_DOWNLOADER = "TAG_PEOPLE_DOWN_SYNC_DOWNLOADER"
        const val TAG_PEOPLE_DOWN_SYNC_COUNTER = "TAG_PEOPLE_DOWN_SYNC_COUNTER"
    }

    suspend fun buildDownSyncWorkerChain(uniqueSyncID: String): List<WorkRequest>
}
