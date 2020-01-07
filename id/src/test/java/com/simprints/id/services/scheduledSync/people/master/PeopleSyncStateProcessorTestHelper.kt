package com.simprints.id.services.scheduledSync.people.master

import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.*
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersFactory
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncCountWorker
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncStateProcessorImplTest.Companion.DOWNLOADED
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncStateProcessorImplTest.Companion.TO_DOWNLOAD
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncStateProcessorImplTest.Companion.TO_UPLOAD
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncStateProcessorImplTest.Companion.UNIQUE_DOWN_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncStateProcessorImplTest.Companion.UNIQUE_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncStateProcessorImplTest.Companion.UNIQUE_UP_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncStateProcessorImplTest.Companion.UPLOADED
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.*
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.Companion.tagForType
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncWorkersBuilder
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncCountWorker
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncUploaderWorker
import java.util.*

fun PeopleSyncState.assertConnectingSyncState() {
    assertProgressAndTotal(syncId, total, progress)
    assertThat(downSyncStates.count { it.state == ENQUEUED }).isEqualTo(1)
    upSyncStates.all { it.state == SUCCEEDED }
}

fun PeopleSyncState.assertFailingSyncState() {
    assertProgressAndTotal(syncId, total, progress)
    assertThat(downSyncStates.count { it.state == FAILED }).isEqualTo(1)
    upSyncStates.all { it.state == SUCCEEDED }
}

fun PeopleSyncState.assertSuccessfulSyncState() {
    assertProgressAndTotal(syncId, total, progress)
    downSyncStates.all { it.state == SUCCEEDED }
    upSyncStates.all { it.state == SUCCEEDED }
}

fun PeopleSyncState.assertRunningSyncState() {
    assertProgressAndTotal(syncId, total, progress)
    assertThat(downSyncStates.count { it.state == RUNNING }).isEqualTo(1)
    upSyncStates.all { it.state == SUCCEEDED }
}

private fun assertProgressAndTotal(syncId: String, total: Int?, progress: Int) {
    assertThat(syncId).isEqualTo(UNIQUE_SYNC_ID)
    assertThat(total).isEqualTo(TO_DOWNLOAD + TO_UPLOAD)
    assertThat(progress).isEqualTo(DOWNLOADED + UPLOADED)
}

fun createWorkInfosHistoryForSuccessfulSyncInMultiAttempts(): List<WorkInfo> {
    val successedWorkInfo = createWorkInfosHistoryForSuccessfulSync()
    return listOf(
        createDownSyncDownloaderWorker(FAILED, UNIQUE_SYNC_ID, ""),
        createDownSyncCounterWorker(FAILED, UNIQUE_SYNC_ID, ""),
        createUpSyncUploaderWorker(FAILED, UNIQUE_SYNC_ID, ""),
        createUpSyncCounterWorker(FAILED, UNIQUE_SYNC_ID, "")
    ) + successedWorkInfo
}

fun createWorkInfosHistoryForSuccessfulSync(): List<WorkInfo> =
    listOf(
        createDownSyncDownloaderWorker(SUCCEEDED, UNIQUE_SYNC_ID),
        createDownSyncCounterWorker(SUCCEEDED, UNIQUE_SYNC_ID),
        createUpSyncUploaderWorker(SUCCEEDED, UNIQUE_SYNC_ID),
        createUpSyncCounterWorker(SUCCEEDED, UNIQUE_SYNC_ID)
    )

fun createWorkInfosHistoryForRunningSync(): List<WorkInfo> =
    listOf(
        createDownSyncDownloaderWorker(RUNNING, UNIQUE_SYNC_ID),
        createDownSyncCounterWorker(SUCCEEDED, UNIQUE_SYNC_ID),
        createUpSyncUploaderWorker(SUCCEEDED, UNIQUE_SYNC_ID),
        createUpSyncCounterWorker(SUCCEEDED, UNIQUE_SYNC_ID)
    )

fun createWorkInfosHistoryForFailingSync(): List<WorkInfo> =
    listOf(
        createDownSyncDownloaderWorker(FAILED, UNIQUE_SYNC_ID),
        createDownSyncCounterWorker(SUCCEEDED, UNIQUE_SYNC_ID),
        createUpSyncUploaderWorker(SUCCEEDED, UNIQUE_SYNC_ID),
        createUpSyncCounterWorker(SUCCEEDED, UNIQUE_SYNC_ID)
    )

fun createWorkInfosHistoryForConnectingSync(): List<WorkInfo> =
    listOf(
        createDownSyncDownloaderWorker(ENQUEUED, UNIQUE_SYNC_ID),
        createDownSyncCounterWorker(SUCCEEDED, UNIQUE_SYNC_ID),
        createUpSyncUploaderWorker(SUCCEEDED, UNIQUE_SYNC_ID),
        createUpSyncCounterWorker(SUCCEEDED, UNIQUE_SYNC_ID)
    )

private fun createDownSyncDownloaderWorker(state: WorkInfo.State,
                                           uniqueMasterSyncId: String?,
                                           uniqueSyncId: String? = UNIQUE_DOWN_SYNC_ID,
                                           id: UUID = UUID.randomUUID()) =
    createWorkInfo(
        state,
        workDataOf(PeopleDownSyncDownloaderWorker.OUTPUT_DOWN_SYNC to DOWNLOADED),
        createCommonDownSyncTags(uniqueMasterSyncId, uniqueSyncId) + listOf(tagForType(DOWNLOADER)),
        workDataOf(PeopleDownSyncDownloaderWorker.PROGRESS_DOWN_SYNC to DOWNLOADED),
        id
    )

private fun createDownSyncCounterWorker(state: WorkInfo.State,
                                        uniqueMasterSyncId: String?,
                                        uniqueSyncId: String? = UNIQUE_DOWN_SYNC_ID,
                                        id: UUID = UUID.randomUUID()) =
    createWorkInfo(
        state,
        workDataOf(PeopleDownSyncCountWorker.OUTPUT_COUNT_WORKER_DOWN to JsonHelper.gson.toJson(listOf(PeopleCount(TO_DOWNLOAD, 0, 0)))),
        createCommonDownSyncTags(uniqueMasterSyncId, uniqueSyncId) + listOf(tagForType(DOWN_COUNTER)),
        workDataOf(),
        id
    )

private fun createUpSyncUploaderWorker(state: WorkInfo.State,
                                       uniqueMasterSyncId: String?,
                                       uniqueSyncId: String? = UNIQUE_UP_SYNC_ID,
                                       id: UUID = UUID.randomUUID()) =
    createWorkInfo(
        state,
        workDataOf(PeopleUpSyncUploaderWorker.OUTPUT_UP_SYNC to UPLOADED),
        createCommonUpSyncTags(uniqueMasterSyncId, uniqueSyncId) + listOf(tagForType(UPLOADER)),
        workDataOf(PeopleUpSyncUploaderWorker.PROGRESS_UP_SYNC to UPLOADED),
        id
    )

private fun createUpSyncCounterWorker(state: WorkInfo.State,
                                      uniqueMasterSyncId: String?,
                                      uniqueSyncId: String? = UNIQUE_UP_SYNC_ID,
                                      id: UUID = UUID.randomUUID()) =
    createWorkInfo(
        state,
        workDataOf(PeopleUpSyncCountWorker.OUTPUT_COUNT_WORKER_UP to JsonHelper.gson.toJson(PeopleCount(TO_UPLOAD, 0, 0))),
        createCommonUpSyncTags(uniqueMasterSyncId, uniqueSyncId) + listOf(tagForType(UP_COUNTER)),
        workDataOf(),
        id
    )

fun createCommonDownSyncTags(uniqueMasterSyncId: String?,
                             uniqueSyncId: String?) = listOf(
    "${PeopleDownSyncWorkersFactory.TAG_DOWN_MASTER_SYNC_ID}${uniqueSyncId}",
    "${PeopleSyncMasterWorker.TAG_SCHEDULED_AT}${Date().time}",
    PeopleDownSyncWorkersFactory.TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS,
    PeopleSyncMasterWorker.TAG_PEOPLE_SYNC_ALL_WORKERS,
    "${PeopleSyncMasterWorker.TAG_MASTER_SYNC_ID}${uniqueMasterSyncId}"
)

private fun createCommonUpSyncTags(uniqueMasterSyncId: String?,
                                   uniqueSyncId: String?) = listOf(
    "${PeopleUpSyncWorkersBuilder.TAG_UP_MASTER_SYNC_ID}${uniqueSyncId}",
    "${PeopleSyncMasterWorker.TAG_SCHEDULED_AT}${Date().time}",
    PeopleUpSyncWorkersBuilder.TAG_PEOPLE_UP_SYNC_ALL_WORKERS,
    PeopleSyncMasterWorker.TAG_PEOPLE_SYNC_ALL_WORKERS,
    "${PeopleSyncMasterWorker.TAG_MASTER_SYNC_ID}${uniqueMasterSyncId}"
)

fun createWorkInfo(state: WorkInfo.State,
                   output: Data = workDataOf(),
                   tags: List<String> = emptyList(),
                   progress: Data = workDataOf(),
                   id: UUID = UUID.randomUUID()) =
    WorkInfo(
        id,
        state,
        output,
        tags,
        progress,
        0
    )
