package com.simprints.id.services.sync.events.up

import androidx.work.*
import com.simprints.id.services.sync.events.common.*
import com.simprints.id.services.sync.events.master.workers.SubjectsSyncMasterWorker
import com.simprints.id.services.sync.events.up.workers.EventUpSyncCountWorker
import com.simprints.id.services.sync.events.up.workers.EventUpSyncUploaderWorker
import java.util.*
import java.util.concurrent.TimeUnit

class SubjectsUpSyncWorkersBuilderImpl : SubjectsUpSyncWorkersBuilder {


    override fun buildUpSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest> {
        val uniqueUpSyncId = UUID.randomUUID().toString()
        return listOf(buildUpSyncWorkers(uniqueSyncId, uniqueUpSyncId)) + buildCountWorker(uniqueSyncId, uniqueUpSyncId)
    }

    private fun buildUpSyncWorkers(uniqueSyncID: String?,
                                   uniqueUpSyncId: String): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(EventUpSyncUploaderWorker::class.java)
            .upSyncWorker(uniqueSyncID, uniqueUpSyncId, getUpSyncWorkerConstraints())
            .addCommonTagForUploaders()
            .build() as OneTimeWorkRequest


    private fun buildCountWorker(uniqueSyncID: String?,
                                 uniqueUpSyncID: String): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(EventUpSyncCountWorker::class.java)
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
                    builder.setBackoffCriteria(BackoffPolicy.LINEAR, SubjectsSyncMasterWorker.MIN_BACKOFF_SECS, TimeUnit.SECONDS)
                }
            }
}
