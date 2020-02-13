package com.simprints.id.services.scheduledSync.people.up.controllers

import androidx.work.*
import com.simprints.id.services.scheduledSync.people.common.*
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncCountWorker
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncUploaderWorker
import java.util.*
import java.util.concurrent.TimeUnit

class PeopleUpSyncWorkersBuilderImpl : PeopleUpSyncWorkersBuilder {


    override fun buildUpSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest> {
        val uniqueUpSyncId = UUID.randomUUID().toString()
        return listOf(buildUpSyncWorkers(uniqueSyncId, uniqueUpSyncId)) + buildCountWorker(uniqueSyncId, uniqueUpSyncId)
    }

    private fun buildUpSyncWorkers(uniqueSyncID: String?,
                                   uniqueUpSyncId: String): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(PeopleUpSyncUploaderWorker::class.java)
            .upSyncWorker(uniqueSyncID, uniqueUpSyncId, getUpSyncWorkerConstraints())
            .addCommonTagForUploaders()
            .build() as OneTimeWorkRequest


    private fun buildCountWorker(uniqueSyncID: String?,
                                 uniqueUpSyncID: String): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(PeopleUpSyncCountWorker::class.java)
            .upSyncWorker(uniqueSyncID, uniqueUpSyncID)
            .addCommonTagForUpCounters()
            .build() as OneTimeWorkRequest

    private fun getUpSyncWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun WorkRequest.Builder<*, *>.upSyncWorker(uniqueMasterSyncId: String?,
                                                       uniqueUpMasterSyncId: String,
                                                       constraints: Constraints = Constraints.Builder().build()) =
        this.setConstraints(constraints)
            .addTagForMasterSyncId(uniqueMasterSyncId)
            .addTagFoUpSyncId(uniqueUpMasterSyncId)
            .addTagForScheduledAtNow()
            .addCommonTagForUpWorkers()
            .addCommonTagForAllSyncWorkers()
            .also { builder ->
                uniqueMasterSyncId?.let {
                    builder.setBackoffCriteria(BackoffPolicy.LINEAR, PeopleSyncMasterWorker.MIN_BACKOFF_SECS, TimeUnit.SECONDS)
                }
            }
}
