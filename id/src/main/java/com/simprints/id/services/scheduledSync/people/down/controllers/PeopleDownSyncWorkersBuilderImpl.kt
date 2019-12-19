package com.simprints.id.services.scheduledSync.people.down.controllers

import androidx.work.*
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncScope
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder.Companion.TAG_DOWN_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder.Companion.TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncCountWorker
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.INPUT_DOWN_SYNC_OPS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.MIN_BACKOFF_SECS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_PEOPLE_SYNC_ALL_WORKERS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_SCHEDULED_AT
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.Companion.tagForType
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.DOWNLOADER
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.DOWN_COUNTER
import java.util.*
import java.util.concurrent.TimeUnit

class PeopleDownSyncWorkersBuilderImpl(val downSyncScopeRepository: PeopleDownSyncScopeRepository) : PeopleDownSyncWorkersBuilder {

    private val downSyncScope: PeopleDownSyncScope
        get() = downSyncScopeRepository.getDownSyncScope()

    override suspend fun buildDownSyncWorkerChain(uniqueSyncId: String?): List<WorkRequest> {
        val downSyncOps = downSyncScopeRepository.getDownSyncOperations(downSyncScope)
        val uniqueDownSyncId = UUID.randomUUID().toString()
        return downSyncOps.map { buildDownSyncWorkers(uniqueSyncId, uniqueDownSyncId, it) } + buildCountWorker(uniqueSyncId, uniqueDownSyncId)
    }

    private fun buildDownSyncWorkers(uniqueSyncID: String?,
                                     uniqueDownSyncID: String,
                                     downSyncOperation: PeopleDownSyncOperation): WorkRequest =
        OneTimeWorkRequest.Builder(PeopleDownSyncDownloaderWorker::class.java)
            .setInputData(workDataOf(INPUT_DOWN_SYNC_OPS to JsonHelper.gson.toJson(downSyncOperation)))
            .setDownSyncWorker(uniqueSyncID, uniqueDownSyncID, getDownSyncWorkerConstraints())
            .addTag(tagForType(DOWNLOADER))
            .build()

    private fun buildCountWorker(uniqueSyncID: String?,
                                 uniqueDownSyncID: String): WorkRequest =
        OneTimeWorkRequest.Builder(PeopleDownSyncCountWorker::class.java)
            .setDownSyncWorker(uniqueSyncID, uniqueDownSyncID, getDownSyncWorkerConstraints())
            .addTag(tagForType(DOWN_COUNTER))
            .build()

    private fun getDownSyncWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun WorkRequest.Builder<*, *>.setDownSyncWorker(uniqueMasterSyncId: String?,
                                                            uniqueDownMasterSyncId: String,
                                                            constraints: Constraints) =
        this.setConstraints(constraints)
            .addTag("${TAG_DOWN_MASTER_SYNC_ID}${uniqueDownMasterSyncId}")
            .addTag("${TAG_SCHEDULED_AT}${Date().time}")
            .addTag(TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS)
            .addTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
            .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_SECS, TimeUnit.SECONDS).also { builder ->
                uniqueMasterSyncId?.let {
                    builder.addTag("${TAG_MASTER_SYNC_ID}${uniqueMasterSyncId}")
                }
            }

}
