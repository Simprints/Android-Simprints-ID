package com.simprints.infra.eventsync.sync.up

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncScope
import com.simprints.infra.eventsync.sync.MIN_BACKOFF_SECS
import com.simprints.infra.eventsync.sync.common.addCommonTagForAllSyncWorkers
import com.simprints.infra.eventsync.sync.common.addCommonTagForUpWorkers
import com.simprints.infra.eventsync.sync.common.addCommonTagForUploaders
import com.simprints.infra.eventsync.sync.common.addTagFoUpSyncId
import com.simprints.infra.eventsync.sync.common.addTagForMasterSyncId
import com.simprints.infra.eventsync.sync.common.addTagForScheduledAtNow
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker.Companion.INPUT_EVENT_UP_SYNC_SCOPE_ID
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker.Companion.INPUT_UP_SYNC
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class EventUpSyncWorkersBuilder @Inject constructor(
    private val upSyncScopeRepository: EventUpSyncScopeRepository,
    private val jsonHelper: JsonHelper,
) {
    suspend fun buildUpSyncWorkerChain(
        uniqueSyncId: String,
        uniqueUpSyncId: String,
    ): List<OneTimeWorkRequest> {
        val upSyncScope = upSyncScopeRepository.getUpSyncScope()

        return listOf(buildUpSyncWorkers(uniqueSyncId, uniqueUpSyncId, upSyncScope))
    }

    private fun buildUpSyncWorkers(
        uniqueSyncID: String?,
        uniqueUpSyncId: String,
        upSyncScope: EventUpSyncScope,
    ): OneTimeWorkRequest = OneTimeWorkRequest
        .Builder(EventUpSyncUploaderWorker::class.java)
        .setInputData(
            workDataOf(
                INPUT_UP_SYNC to jsonHelper.toJson(upSyncScope),
                INPUT_EVENT_UP_SYNC_SCOPE_ID to uniqueUpSyncId,
            ),
        ).upSyncWorker(uniqueSyncID, uniqueUpSyncId, getUpSyncWorkerConstraints())
        .addCommonTagForUploaders()
        .build() as OneTimeWorkRequest

    private fun getUpSyncWorkerConstraints() = Constraints
        .Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private fun WorkRequest.Builder<*, *>.upSyncWorker(
        uniqueMasterSyncId: String?,
        uniqueUpMasterSyncId: String,
        constraints: Constraints = Constraints.Builder().build(),
    ) = this
        .setConstraints(constraints)
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
                    TimeUnit.SECONDS,
                )
            }
        }
}
