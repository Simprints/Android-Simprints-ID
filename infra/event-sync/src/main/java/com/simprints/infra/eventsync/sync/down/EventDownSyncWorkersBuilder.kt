package com.simprints.infra.eventsync.sync.down

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.simprints.core.domain.tokenization.values
import com.simprints.core.tools.json.JsonHelper
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
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class EventDownSyncWorkersBuilder @Inject constructor(
    private val downSyncScopeRepository: EventDownSyncScopeRepository,
    private val jsonHelper: JsonHelper,
    private val configRepository: ConfigRepository,
) {

    suspend fun buildDownSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest> {
        val projectConfiguration = configRepository.getProjectConfiguration()
        val deviceConfiguration = configRepository.getDeviceConfiguration()

        val downSyncScope = downSyncScopeRepository.getDownSyncScope(
            modes = projectConfiguration.general.modalities.map { it.toMode() },
            selectedModuleIDs = deviceConfiguration.selectedModules.values(),
            syncPartitioning = projectConfiguration.synchronization.down.partitionType.toDomain()
        )

        val uniqueDownSyncId = UUID.randomUUID().toString()
        return downSyncScope.operations.map { buildDownSyncWorkers(uniqueSyncId, uniqueDownSyncId, it) }
    }

    private fun buildDownSyncWorkers(
        uniqueSyncID: String?,
        uniqueDownSyncID: String,
        downSyncOperation: EventDownSyncOperation
    ): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(EventDownSyncDownloaderWorker::class.java)
            .setInputData(workDataOf(INPUT_DOWN_SYNC_OPS to jsonHelper.toJson(downSyncOperation)))
            .setDownSyncWorker(uniqueSyncID, uniqueDownSyncID, getDownSyncWorkerConstraints())
            .addCommonTagForDownloaders()
            .build() as OneTimeWorkRequest

    private fun getDownSyncWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun WorkRequest.Builder<*, *>.setDownSyncWorker(
        uniqueMasterSyncId: String?,
        uniqueDownMasterSyncId: String,
        constraints: Constraints
    ) =
        this.setConstraints(constraints)
            .addTagForMasterSyncId(uniqueMasterSyncId)
            .addTagForDownSyncId(uniqueDownMasterSyncId)
            .addTagForScheduledAtNow()
            .addCommonTagForDownWorkers()
            .addCommonTagForAllSyncWorkers()
            .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_SECS, TimeUnit.SECONDS)

}
