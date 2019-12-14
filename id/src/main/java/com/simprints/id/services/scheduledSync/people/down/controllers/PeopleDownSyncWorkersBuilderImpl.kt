package com.simprints.id.services.scheduledSync.people.down.controllers

import androidx.work.*
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.people_sync.down.DownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncScope
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder.Companion.TAG_DOWN_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder.Companion.TAG_PEOPLE_DOWN_SYNC_ALL_COUNTERS
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder.Companion.TAG_PEOPLE_DOWN_SYNC_ALL_DOWNLOADERS
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder.Companion.TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncCountWorker
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.MIN_BACKOFF_MILLIS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_PEOPLE_SYNC_ALL_WORKERS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_SCHEDULED_AT
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType
import java.util.*
import java.util.concurrent.TimeUnit

class PeopleDownSyncWorkersBuilderImpl(val downSyncScopeRepository: DownSyncScopeRepository) : PeopleDownSyncWorkersBuilder {

    private val downSyncScope: PeopleDownSyncScope
        get() = downSyncScopeRepository.getDownSyncScope()

    override suspend fun buildDownSyncWorkerChain(uniqueSyncId: String?): List<WorkRequest> {
        val downSyncOps = downSyncScopeRepository.getDownSyncOperations(downSyncScope)
        val uniqueDownSyncId = UUID.randomUUID().toString()
        return downSyncOps.map { buildDownSyncWorkers(uniqueSyncId, uniqueDownSyncId, it) } + listOf(buildCountWorker(uniqueSyncId, uniqueDownSyncId))
    }

    private fun buildDownSyncWorkers(uniqueSyncID: String?,
                                     uniqueDownSyncID: String,
                                     downSyncOperation: PeopleDownSyncOperation): WorkRequest =
        OneTimeWorkRequest.Builder(PeopleDownSyncDownloaderWorker::class.java)
            .setInputData(workDataOf(INPUT_DOWN_SYNC_OPS to JsonHelper.gson.toJson(downSyncOperation)))
            .setDownSyncWorker(uniqueSyncID, uniqueDownSyncID, getDownSyncWorkerConstraints())
            .addTag(PeopleSyncWorkerType.tagForType(PeopleSyncWorkerType.DOWNLOADER))
            .addTag(TAG_PEOPLE_DOWN_SYNC_ALL_DOWNLOADERS)
            .build()

    private fun buildCountWorker(uniqueSyncID: String?,
                                 uniqueDownSyncID: String): WorkRequest =
        OneTimeWorkRequest.Builder(PeopleDownSyncCountWorker::class.java)
            .setDownSyncWorker(uniqueSyncID, uniqueDownSyncID, getDownSyncWorkerConstraints())
            .addTag(PeopleSyncWorkerType.tagForType(PeopleSyncWorkerType.COUNTER))
            .addTag(TAG_PEOPLE_DOWN_SYNC_ALL_COUNTERS)
            .build()

    private fun getDownSyncWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun WorkRequest.Builder<*, *>.setDownSyncWorker(uniqueMasterSyncID: String?,
                                                            uniqueDownMasterSyncID: String,
                                                            constraints: Constraints) =
        this.setConstraints(constraints)
            .addTag("${TAG_DOWN_MASTER_SYNC_ID}${uniqueDownMasterSyncID}")
            .addTag("${TAG_SCHEDULED_AT}${Date().time}")
            .addTag(TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS)
            .addTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
            .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_MILLIS, TimeUnit.SECONDS).also { builder ->
                uniqueMasterSyncID?.let {
                    builder.addTag("${TAG_MASTER_SYNC_ID}${uniqueMasterSyncID}")
                }
            }

}
