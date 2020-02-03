package com.simprints.id.services.scheduledSync.people.down.controllers

import androidx.work.*
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncScope
import com.simprints.id.services.scheduledSync.people.common.*
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncCountWorker
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker.Companion.MIN_BACKOFF_SECS
import java.util.*
import java.util.concurrent.TimeUnit

class PeopleDownSyncWorkersBuilderImpl(val downSyncScopeRepository: PeopleDownSyncScopeRepository) : PeopleDownSyncWorkersBuilder {

    private val downSyncScope: PeopleDownSyncScope
        get() = downSyncScopeRepository.getDownSyncScope()

    override suspend fun buildDownSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest> {
        val downSyncOps = downSyncScopeRepository.getDownSyncOperations(downSyncScope)
        val uniqueDownSyncId = UUID.randomUUID().toString()
        return downSyncOps.map { buildDownSyncWorkers(uniqueSyncId, uniqueDownSyncId, it) } + buildCountWorker(uniqueSyncId, uniqueDownSyncId)
    }

    private fun buildDownSyncWorkers(uniqueSyncID: String?,
                                     uniqueDownSyncID: String,
                                     downSyncOperation: PeopleDownSyncOperation): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(PeopleDownSyncDownloaderWorker::class.java)
            .setInputData(workDataOf(INPUT_DOWN_SYNC_OPS to JsonHelper.gson.toJson(downSyncOperation)))
            .setDownSyncWorker(uniqueSyncID, uniqueDownSyncID, getDownSyncWorkerConstraints())
            .addCommonTagForDownloaders()
            .build() as OneTimeWorkRequest

    private fun buildCountWorker(uniqueSyncID: String?,
                                 uniqueDownSyncID: String): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(PeopleDownSyncCountWorker::class.java)
            .setDownSyncWorker(uniqueSyncID, uniqueDownSyncID, getDownSyncWorkerConstraints())
            .addCommonTagForDownCounters()
            .build() as OneTimeWorkRequest

    private fun getDownSyncWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun WorkRequest.Builder<*, *>.setDownSyncWorker(uniqueMasterSyncId: String?,
                                                            uniqueDownMasterSyncId: String,
                                                            constraints: Constraints) =
        this.setConstraints(constraints)
            .addTagForMasterSyncId(uniqueMasterSyncId)
            .addTagForDownSyncId(uniqueDownMasterSyncId)
            .addTagForScheduledAtNow()
            .addCommonTagForDownWorkers()
            .addCommonTagForAllSyncWorkers()
            .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_SECS, TimeUnit.SECONDS)
}
