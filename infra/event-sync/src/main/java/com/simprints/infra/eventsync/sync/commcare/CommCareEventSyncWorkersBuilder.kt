package com.simprints.infra.eventsync.sync.commcare

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.simprints.core.domain.tokenization.values
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.sync.MIN_BACKOFF_SECS
import com.simprints.infra.eventsync.sync.commcare.workers.CommCareEventSyncDownloaderWorker
import com.simprints.infra.eventsync.sync.common.addCommonTagForAllSyncWorkers
import com.simprints.infra.eventsync.sync.common.addCommonTagForDownWorkers
import com.simprints.infra.eventsync.sync.common.addCommonTagForDownloaders
import com.simprints.infra.eventsync.sync.common.addTagForDownSyncId
import com.simprints.infra.eventsync.sync.common.addTagForMasterSyncId
import com.simprints.infra.eventsync.sync.common.addTagForScheduledAtNow
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.INPUT_EVENT_DOWN_SYNC_SCOPE_ID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class CommCareEventSyncWorkersBuilder @Inject constructor(
    private val downSyncScopeRepository: EventDownSyncScopeRepository,
    private val jsonHelper: JsonHelper,
    private val configManager: ConfigManager,
) {
    suspend fun buildDownSyncWorkerChain(
        uniqueSyncId: String,
        uniqueDownSyncId: String,
    ): List<OneTimeWorkRequest> {
        val projectConfiguration = configManager.getProjectConfiguration()
        val deviceConfiguration = configManager.getDeviceConfiguration()

        //TODO(milen): downSyncScope is not used in CommCare ATM, remove if not needed
        val downSyncScope = downSyncScopeRepository.getDownSyncScope(
            modes = projectConfiguration.general.modalities.map { it.toMode() },
            selectedModuleIDs = deviceConfiguration.selectedModules.values(),
            syncPartitioning = projectConfiguration.synchronization.down.partitionType
                .toDomain(),
        )

        return downSyncScope.operations.map {
            buildDownSyncWorkers(uniqueSyncId, uniqueDownSyncId, it)
        }
    }

    private fun buildDownSyncWorkers(
        uniqueSyncID: String,
        uniqueDownSyncID: String,
        downSyncOperation: EventDownSyncOperation,
    ): OneTimeWorkRequest = OneTimeWorkRequest
        .Builder(CommCareEventSyncDownloaderWorker::class.java)
        .setInputData(
            workDataOf(
                INPUT_DOWN_SYNC_OPS to jsonHelper.toJson(downSyncOperation),
                INPUT_EVENT_DOWN_SYNC_SCOPE_ID to uniqueDownSyncID,
            ),
        ).setDownSyncWorker(uniqueSyncID, uniqueDownSyncID, getDownSyncWorkerConstraints())
        .addCommonTagForDownloaders()
        .build() as OneTimeWorkRequest

    private fun getDownSyncWorkerConstraints() = Constraints
        .Builder()
        .build()

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
