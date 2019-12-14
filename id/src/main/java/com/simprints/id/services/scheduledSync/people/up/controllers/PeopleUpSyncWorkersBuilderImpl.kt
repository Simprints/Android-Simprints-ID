package com.simprints.id.services.scheduledSync.people.up.controllers

import androidx.work.*
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_PEOPLE_SYNC_WORKERS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_SCHEDULED_AT
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncWorkersBuilder.Companion.TAG_UP_SYNC_UPLOADER
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncWorkersBuilder.Companion.TAG_UP_SYNC_WORKERS
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncUploaderWorker
import java.util.*
import java.util.concurrent.TimeUnit

class PeopleUpSyncWorkersBuilderImpl(): PeopleUpSyncWorkersBuilder {

    override fun buildUpSyncWorkerChain(uniqueSyncID: String): List<WorkRequest> =
        listOf(buildUpSyncWorkers(uniqueSyncID))


    private fun buildUpSyncWorkers(uniqueSyncID: String): WorkRequest =
        OneTimeWorkRequest.Builder(PeopleUpSyncUploaderWorker::class.java)
            .upDownSyncWorker(uniqueSyncID, getUpSyncWorkerConstraints())
            .addTag(TAG_UP_SYNC_UPLOADER)
            .build()


    private fun getUpSyncWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun WorkRequest.Builder<*, *>.upDownSyncWorker(uniqueSyncID: String, constraints: Constraints) =
        this.setConstraints(constraints)
            .addTag(uniqueSyncID)
            .addTag("${TAG_SCHEDULED_AT}${uniqueSyncID}")
            .addTag("${TAG_SCHEDULED_AT}${Date().time}")
            .addTag(TAG_UP_SYNC_WORKERS)
            .addTag(TAG_PEOPLE_SYNC_WORKERS)
            .setBackoffCriteria(BackoffPolicy.LINEAR, PeopleSyncMasterWorker.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)

}
