package com.simprints.infra.eventsync.sync.up

import androidx.work.*
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncScope
import com.simprints.infra.eventsync.sync.MIN_BACKOFF_SECS
import com.simprints.infra.eventsync.sync.common.*
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncCountWorker
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncCountWorker.Companion.INPUT_COUNT_WORKER_UP
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker.Companion.INPUT_UP_SYNC
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EventUpSyncWorkersBuilder @Inject constructor(
    private val upSyncScopeRepository: EventUpSyncScopeRepository,
    private val jsonHelper: JsonHelper
) {

    suspend fun buildUpSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest> {
        val uniqueUpSyncId = UUID.randomUUID().toString()
        val upSyncScope = upSyncScopeRepository.getUpSyncScope()
        return listOf(
            buildUpSyncWorkers(
                uniqueSyncId,
                uniqueUpSyncId,
                upSyncScope
            )
        ) + buildCountWorker(uniqueSyncId, uniqueUpSyncId, upSyncScope)
    }

    private fun buildUpSyncWorkers(
        uniqueSyncID: String?,
        uniqueUpSyncId: String,
        upSyncScope: EventUpSyncScope
    ): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(EventUpSyncUploaderWorker::class.java)
            .setInputData(workDataOf(INPUT_UP_SYNC to jsonHelper.toJson(upSyncScope)))
            .upSyncWorker(uniqueSyncID, uniqueUpSyncId, getUpSyncWorkerConstraints())
            .addCommonTagForUploaders()
            .build() as OneTimeWorkRequest


    private fun buildCountWorker(
        uniqueSyncID: String?,
        uniqueUpSyncID: String,
        upSyncScope: EventUpSyncScope
    ): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(EventUpSyncCountWorker::class.java)
            .setInputData(workDataOf(INPUT_COUNT_WORKER_UP to jsonHelper.toJson(upSyncScope)))
            .upSyncWorker(uniqueSyncID, uniqueUpSyncID)
            .addCommonTagForUpCounters()
            .build() as OneTimeWorkRequest

    private fun getUpSyncWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun WorkRequest.Builder<*, *>.upSyncWorker(
        uniqueMasterSyncId: String?,
        uniqueUpMasterSyncId: String,
        constraints: Constraints = Constraints.Builder().build()
    ) =
        this.setConstraints(constraints)
            .addTagForMasterSyncId(uniqueMasterSyncId)
            .addTagFoUpSyncId(uniqueUpMasterSyncId)
            .addTagForScheduledAtNow()
            .addCommonTagForUpWorkers()
            .addCommonTagForAllSyncWorkers()
            .also { builder ->
                uniqueMasterSyncId?.let {
                    builder.setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        MIN_BACKOFF_SECS,
                        TimeUnit.SECONDS
                    )
                }
            }
}
