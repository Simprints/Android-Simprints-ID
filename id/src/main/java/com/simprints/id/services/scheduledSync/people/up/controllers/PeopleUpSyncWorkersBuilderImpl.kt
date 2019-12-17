package com.simprints.id.services.scheduledSync.people.up.controllers

import androidx.work.*
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_PEOPLE_SYNC_ALL_WORKERS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_SCHEDULED_AT
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.Companion.tagForType
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.UPLOADER
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.UP_COUNTER
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncWorkersBuilder.Companion.TAG_PEOPLE_UP_SYNC_ALL_COUNTERS
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncWorkersBuilder.Companion.TAG_PEOPLE_UP_SYNC_ALL_UPLOADERS
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncWorkersBuilder.Companion.TAG_PEOPLE_UP_SYNC_ALL_WORKERS
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncWorkersBuilder.Companion.TAG_UP_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncCountWorker
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncUploaderWorker
import java.util.*
import java.util.concurrent.TimeUnit

class PeopleUpSyncWorkersBuilderImpl : PeopleUpSyncWorkersBuilder {


    override fun buildUpSyncWorkerChain(uniqueSyncId: String?): List<WorkRequest> {
        val uniqueUpSyncId = UUID.randomUUID().toString()
        return listOf(buildUpSyncWorkers(uniqueSyncId, uniqueUpSyncId)) + buildCountWorker(uniqueSyncId, uniqueUpSyncId)
    }

    private fun buildUpSyncWorkers(uniqueSyncID: String?,
                                   uniqueUpSyncId: String): WorkRequest =
        OneTimeWorkRequest.Builder(PeopleUpSyncUploaderWorker::class.java)
            .upSyncWorker(uniqueSyncID, uniqueUpSyncId, getUpSyncWorkerConstraints())
            .addTag(tagForType(UPLOADER))
            .addTag(TAG_PEOPLE_UP_SYNC_ALL_UPLOADERS)
            .build()


    private fun buildCountWorker(uniqueSyncID: String?,
                                 uniqueUpSyncID: String): WorkRequest =
        OneTimeWorkRequest.Builder(PeopleUpSyncCountWorker::class.java)
            .upSyncWorker(uniqueSyncID, uniqueUpSyncID)
            .addTag(tagForType(UP_COUNTER))
            .addTag(TAG_PEOPLE_UP_SYNC_ALL_COUNTERS)
            .build()

    private fun getUpSyncWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun WorkRequest.Builder<*, *>.upSyncWorker(uniqueMasterSyncId: String?,
                                                       uniqueUpMasterSyncId: String,
                                                       constraints: Constraints = Constraints.Builder().build()) =
        this.setConstraints(constraints)
            .addTag("${TAG_UP_MASTER_SYNC_ID}${uniqueUpMasterSyncId}")
            .addTag("${TAG_SCHEDULED_AT}${Date().time}")
            .addTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
            .addTag(TAG_PEOPLE_UP_SYNC_ALL_WORKERS)
            .setBackoffCriteria(BackoffPolicy.LINEAR, PeopleSyncMasterWorker.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS).also { builder ->
                uniqueMasterSyncId?.let {
                    builder.addTag("${TAG_MASTER_SYNC_ID}${uniqueMasterSyncId}")
                }
            }
}
