package com.simprints.id.services.scheduledSync.people.down.controllers

import androidx.work.*
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.people_sync.down.DownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncScope
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkerBuilder.Companion.TAG_PEOPLE_DOWN_SYNC_COUNTER
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkerBuilder.Companion.TAG_PEOPLE_DOWN_SYNC_DOWNLOADER
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkerBuilder.Companion.TAG_PEOPLE_DOWN_SYNC_WORKERS
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncCountWorker
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.MIN_BACKOFF_MILLIS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_PEOPLE_SYNC_WORKERS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_SCHEDULED_AT
import java.util.*
import java.util.concurrent.TimeUnit

class PeopleDownSyncWorkerBuilderImpl(val downSyncScopeRepository: DownSyncScopeRepository) : PeopleDownSyncWorkerBuilder {

    private val downSyncScope: PeopleDownSyncScope
        get() = downSyncScopeRepository.getDownSyncScope()

    override suspend fun buildDownSyncWorkerChain(uniqueSyncID: String): List<WorkRequest> {
        val downSyncOps = downSyncScopeRepository.getDownSyncOperations(downSyncScope)
        return downSyncOps.map { buildDownSyncWorkers(uniqueSyncID, it) } + listOf(buildCountWorker(uniqueSyncID))
    }

    private fun buildDownSyncWorkers(uniqueSyncID: String, downSyncOperation: PeopleDownSyncOperation): WorkRequest =
        OneTimeWorkRequest.Builder(PeopleDownSyncDownloaderWorker::class.java)
            .setInputData(workDataOf(INPUT_DOWN_SYNC_OPS to JsonHelper.gson.toJson(downSyncOperation)))
            .setDownSyncWorker(uniqueSyncID, getDownSyncWorkerConstraints())
            .addTag(TAG_PEOPLE_DOWN_SYNC_DOWNLOADER)
            .build()

    private fun buildCountWorker(uniqueSyncID: String): WorkRequest =
        OneTimeWorkRequest.Builder(PeopleDownSyncCountWorker::class.java)
            .setDownSyncWorker(uniqueSyncID, getDownSyncWorkerConstraints())
            .addTag(TAG_PEOPLE_DOWN_SYNC_COUNTER)
            .build()

    private fun getDownSyncWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun WorkRequest.Builder<*, *>.setDownSyncWorker(uniqueSyncID: String, constraints: Constraints) =
        this.setConstraints(constraints)
            .addTag("${TAG_SCHEDULED_AT}${uniqueSyncID}")
            .addTag("${TAG_SCHEDULED_AT}${Date().time}")
            .addTag(TAG_PEOPLE_DOWN_SYNC_WORKERS)
            .addTag(TAG_PEOPLE_SYNC_WORKERS)
            .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)

}
