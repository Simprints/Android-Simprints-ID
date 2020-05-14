package com.simprints.id.services.scheduledSync.subjects.down.controllers

import androidx.work.*
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperation
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncScope
import com.simprints.id.services.scheduledSync.subjects.common.*
import com.simprints.id.services.scheduledSync.subjects.down.workers.SubjectsDownSyncCountWorker
import com.simprints.id.services.scheduledSync.subjects.down.workers.SubjectsDownSyncDownloaderWorker
import com.simprints.id.services.scheduledSync.subjects.down.workers.SubjectsDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.id.services.scheduledSync.subjects.master.workers.SubjectsSyncMasterWorker.Companion.MIN_BACKOFF_SECS
import java.util.*
import java.util.concurrent.TimeUnit

class SubjectsDownSyncWorkersBuilderImpl(val downSyncScopeRepository: SubjectsDownSyncScopeRepository) : SubjectsDownSyncWorkersBuilder {

    private val downSyncScope: SubjectsDownSyncScope
        get() = downSyncScopeRepository.getDownSyncScope()

    override suspend fun buildDownSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest> {
        val downSyncOps = downSyncScopeRepository.getDownSyncOperations(downSyncScope)
        val uniqueDownSyncId = UUID.randomUUID().toString()
        return downSyncOps.map { buildDownSyncWorkers(uniqueSyncId, uniqueDownSyncId, it) } + buildCountWorker(uniqueSyncId, uniqueDownSyncId)
    }

    private fun buildDownSyncWorkers(uniqueSyncID: String?,
                                     uniqueDownSyncID: String,
                                     downSyncOperation: SubjectsDownSyncOperation): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(SubjectsDownSyncDownloaderWorker::class.java)
            .setInputData(workDataOf(INPUT_DOWN_SYNC_OPS to JsonHelper.gson.toJson(downSyncOperation)))
            .setDownSyncWorker(uniqueSyncID, uniqueDownSyncID, getDownSyncWorkerConstraints())
            .addCommonTagForDownloaders()
            .build() as OneTimeWorkRequest

    private fun buildCountWorker(uniqueSyncID: String?,
                                 uniqueDownSyncID: String): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(SubjectsDownSyncCountWorker::class.java)
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
