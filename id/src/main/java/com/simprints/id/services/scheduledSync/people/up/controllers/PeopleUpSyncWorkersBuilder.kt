package com.simprints.id.services.scheduledSync.people.up.controllers

import androidx.work.OneTimeWorkRequest
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker.Companion.TAG_PEOPLE_SYNC_ALL_WORKERS

interface PeopleUpSyncWorkersBuilder {

    companion object {

        const val TAG_UP_MASTER_SYNC_ID = "TAG_UP_MASTER_SYNC_ID"
        const val TAG_PEOPLE_UP_SYNC_ALL_WORKERS = "UP_${TAG_PEOPLE_SYNC_ALL_WORKERS}"
        const val TAG_PEOPLE_UP_SYNC_ALL_UPLOADERS = "TAG_PEOPLE_UP_SYNC_ALL_UPLOADERS"
        const val TAG_PEOPLE_UP_SYNC_ALL_COUNTERS = "TAG_PEOPLE_UP_SYNC_ALL_COUNTERS"
    }

    fun buildUpSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest>
}
