package com.simprints.infra.eventsync.sync.down

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.sync.MIN_BACKOFF_SECS
import com.simprints.infra.eventsync.sync.common.addCommonTagForAllSyncWorkers
import com.simprints.infra.eventsync.sync.common.addCommonTagForDownWorkers
import com.simprints.infra.eventsync.sync.common.addCommonTagForDownloaders
import com.simprints.infra.eventsync.sync.common.addTagForDownSyncId
import com.simprints.infra.eventsync.sync.common.addTagForMasterSyncId
import com.simprints.infra.eventsync.sync.common.addTagForScheduledAtNow
import com.simprints.infra.eventsync.sync.down.workers.BaseEventDownSyncDownloaderWorker
import com.simprints.infra.eventsync.sync.down.workers.BaseEventDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.infra.eventsync.sync.down.workers.BaseEventDownSyncDownloaderWorker.Companion.INPUT_EVENT_DOWN_SYNC_SCOPE_ID
import com.simprints.infra.serialization.SimJson
import java.util.concurrent.TimeUnit

internal abstract class BaseEventDownSyncWorkersBuilder(
    protected val downSyncScopeRepository: EventDownSyncScopeRepository,
    protected val configRepository: ConfigRepository,
) {
    abstract fun getWorkerClass(): Class<out BaseEventDownSyncDownloaderWorker>

    abstract fun getDownSyncWorkerConstraints(): Constraints

    abstract suspend fun buildDownSyncWorkerChain(
        uniqueSyncId: String,
        uniqueDownSyncId: String,
    ): List<OneTimeWorkRequest>

    protected fun buildDownSyncWorkers(
        uniqueSyncID: String,
        uniqueDownSyncID: String,
        downSyncOperation: EventDownSyncOperation,
    ): OneTimeWorkRequest = OneTimeWorkRequest
        .Builder(getWorkerClass())
        .setInputData(
            workDataOf(
                INPUT_DOWN_SYNC_OPS to SimJson.encodeToString(downSyncOperation),
                INPUT_EVENT_DOWN_SYNC_SCOPE_ID to uniqueDownSyncID,
            ),
        ).setDownSyncWorker(uniqueSyncID, uniqueDownSyncID, getDownSyncWorkerConstraints())
        .addCommonTagForDownloaders()
        .build() as OneTimeWorkRequest

    private fun WorkRequest.Builder<*, *>.setDownSyncWorker(
        uniqueMasterSyncId: String,
        uniqueDownMasterSyncId: String,
        constraints: Constraints,
    ) = this
        .setConstraints(constraints)
        .addTagForMasterSyncId(uniqueMasterSyncId)
        .addTagForDownSyncId(uniqueDownMasterSyncId)
        .addTagForScheduledAtNow()
        .addCommonTagForDownWorkers()
        .addCommonTagForAllSyncWorkers()
        .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_SECS, TimeUnit.SECONDS)
}
