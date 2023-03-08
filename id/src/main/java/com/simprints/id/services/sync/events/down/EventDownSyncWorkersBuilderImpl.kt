package com.simprints.id.services.sync.events.down

import androidx.work.*
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.services.sync.events.common.*
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker.Companion.INPUT_COUNT_WORKER_DOWN
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.id.services.sync.events.master.workers.EventSyncMasterWorker.Companion.MIN_BACKOFF_SECS
import com.simprints.id.tools.extensions.toGroup
import com.simprints.id.tools.extensions.toMode
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.events.events_sync.down.EventDownSyncScopeRepository
import com.simprints.infra.events.events_sync.down.domain.EventDownSyncOperation
import com.simprints.infra.events.events_sync.down.domain.EventDownSyncScope
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EventDownSyncWorkersBuilderImpl @Inject constructor(
    private val downSyncScopeRepository: EventDownSyncScopeRepository,
    private val jsonHelper: JsonHelper,
    private val configManager: ConfigManager,
) : EventDownSyncWorkersBuilder {

    override suspend fun buildDownSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest> {
        val projectConfiguration = configManager.getProjectConfiguration()
        val deviceConfiguration = configManager.getDeviceConfiguration()

        val downSyncScope = downSyncScopeRepository.getDownSyncScope(
            projectConfiguration.general.modalities.map { it.toMode() },
            deviceConfiguration.selectedModules,
            projectConfiguration.synchronization.down.partitionType.toGroup()
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
