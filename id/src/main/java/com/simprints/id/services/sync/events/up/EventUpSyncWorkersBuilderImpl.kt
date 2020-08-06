package com.simprints.id.services.sync.events.up

import androidx.work.*
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.events_sync.up.EventUpSyncScopeRepository
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncScope
import com.simprints.id.services.sync.events.common.*
import com.simprints.id.services.sync.events.master.workers.SubjectsSyncMasterWorker
import com.simprints.id.services.sync.events.up.workers.EventUpSyncCountWorker
import com.simprints.id.services.sync.events.up.workers.EventUpSyncCountWorker.Companion.INPUT_COUNT_WORKER_UP
import com.simprints.id.services.sync.events.up.workers.EventUpSyncUploaderWorker
import com.simprints.id.services.sync.events.up.workers.EventUpSyncUploaderWorker.Companion.INPUT_UP_SYNC
import java.util.*
import java.util.concurrent.TimeUnit

class EventUpSyncWorkersBuilderImpl(private val upSyncScopeRepository: EventUpSyncScopeRepository,
                                    private val jsonHelper: JsonHelper) : EventUpSyncWorkersBuilder {


    override suspend fun buildUpSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest> {
        val uniqueUpSyncId = UUID.randomUUID().toString()
        val upSyncScope = upSyncScopeRepository.getUpSyncScope()
        return listOf(buildUpSyncWorkers(uniqueSyncId, uniqueUpSyncId, upSyncScope)) + buildCountWorker(uniqueSyncId, uniqueUpSyncId, upSyncScope)
    }

    private fun buildUpSyncWorkers(uniqueSyncID: String?,
                                   uniqueUpSyncId: String,
                                   upSyncScope: EventUpSyncScope): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(EventUpSyncUploaderWorker::class.java)
            .setInputData(workDataOf(INPUT_UP_SYNC to jsonHelper.toJson(upSyncScope)))
            .upSyncWorker(uniqueSyncID, uniqueUpSyncId, getUpSyncWorkerConstraints())
            .addCommonTagForUploaders()
            .build() as OneTimeWorkRequest


    private fun buildCountWorker(uniqueSyncID: String?,
                                 uniqueUpSyncID: String,
                                 upSyncScope: EventUpSyncScope): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(EventUpSyncCountWorker::class.java)
            .setInputData(workDataOf(INPUT_COUNT_WORKER_UP to jsonHelper.toJson(upSyncScope)))
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
