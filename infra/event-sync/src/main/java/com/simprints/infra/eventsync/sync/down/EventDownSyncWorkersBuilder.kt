package com.simprints.infra.eventsync.sync.down

import androidx.work.*
import com.simprints.core.domain.tokenization.values
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope
import com.simprints.infra.eventsync.sync.MIN_BACKOFF_SECS
import com.simprints.infra.eventsync.sync.common.*
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncCountWorker
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncCountWorker.Companion.INPUT_COUNT_WORKER_DOWN
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class EventDownSyncWorkersBuilder @Inject constructor(
    private val downSyncScopeRepository: EventDownSyncScopeRepository,
    private val jsonHelper: JsonHelper,
    private val configRepository: ConfigRepository,
) {

    suspend fun buildDownSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest> {
        val projectConfiguration = configRepository.getConfiguration()
        val deviceConfiguration = configRepository.getDeviceConfiguration()

        val downSyncScope = downSyncScopeRepository.getDownSyncScope(
            modes = projectConfiguration.general.modalities.map { it.toMode() },
            selectedModuleIDs = deviceConfiguration.selectedModules.values(),
            syncPartitioning = projectConfiguration.synchronization.down.partitionType.toDomain()
        )

        val uniqueDownSyncId = UUID.randomUUID().toString()
        return downSyncScope.operations.map {
            buildDownSyncWorkers(uniqueSyncId, uniqueDownSyncId, it)
        } + buildCountWorker(uniqueSyncId, uniqueDownSyncId, downSyncScope)
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

    private fun buildCountWorker(
        uniqueSyncID: String?,
        uniqueDownSyncID: String,
        eventDownSyncScope: EventDownSyncScope
    ): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(EventDownSyncCountWorker::class.java)
            .setInputData(workDataOf(INPUT_COUNT_WORKER_DOWN to jsonHelper.toJson(eventDownSyncScope)))
            .setDownSyncWorker(uniqueSyncID, uniqueDownSyncID, getDownSyncWorkerConstraints())
            .addCommonTagForDownCounters()
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
