package com.simprints.id.services.sync.events.master

import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.*
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.EventCount
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_CREATION
import com.simprints.id.services.sync.events.common.*
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImplTest.Companion.DOWNLOADED
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImplTest.Companion.TO_DOWNLOAD
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImplTest.Companion.TO_UPLOAD
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImplTest.Companion.UNIQUE_DOWN_SYNC_ID
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImplTest.Companion.UNIQUE_SYNC_ID
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImplTest.Companion.UNIQUE_UP_SYNC_ID
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImplTest.Companion.UPLOADED
import com.simprints.id.services.sync.events.master.models.EventSyncState
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState.*
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.*
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.id.services.sync.events.up.workers.EventUpSyncCountWorker
import com.simprints.id.services.sync.events.up.workers.EventUpSyncUploaderWorker
import java.util.*

fun EventSyncState.assertConnectingSyncState() {
    assertProgressAndTotal(syncId, total, progress)
    assertThat(downSyncWorkersInfo.count { it.state is Enqueued }).isEqualTo(1)
    upSyncWorkersInfo.all { it.state is Succeeded }
}

fun EventSyncState.assertFailingSyncState() {
    assertProgressAndTotal(syncId, total, progress)
    assertThat(downSyncWorkersInfo.count { it.state is Failed }).isEqualTo(1)
    upSyncWorkersInfo.all { it.state is Succeeded }
}

fun EventSyncState.assertSuccessfulSyncState() {
    assertProgressAndTotal(syncId, total, progress)
    downSyncWorkersInfo.all { it.state is Succeeded }
    upSyncWorkersInfo.all { it.state is Succeeded }
}

fun EventSyncState.assertRunningSyncState() {
    assertProgressAndTotal(syncId, total, progress)
    assertThat(downSyncWorkersInfo.count { it.state is Running }).isEqualTo(1)
    upSyncWorkersInfo.all { it.state is Succeeded }
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
        workDataOf(EventDownSyncDownloaderWorker.OUTPUT_DOWN_SYNC to DOWNLOADED),
        createCommonDownSyncTags(uniqueMasterSyncId, uniqueSyncId) + listOf(tagForType(DOWNLOADER)),
        workDataOf(EventDownSyncDownloaderWorker.PROGRESS_DOWN_SYNC to DOWNLOADED),
        id
    )

private fun createDownSyncCounterWorker(state: WorkInfo.State,
                                        uniqueMasterSyncId: String?,
                                        uniqueSyncId: String? = UNIQUE_DOWN_SYNC_ID,
                                        id: UUID = UUID.randomUUID()) =
    createWorkInfo(
        state,
        workDataOf(EventDownSyncCountWorker.OUTPUT_COUNT_WORKER_DOWN to JsonHelper().toJson(EventCount(ENROLMENT_RECORD_CREATION, TO_DOWNLOAD))),
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
        workDataOf(EventUpSyncUploaderWorker.OUTPUT_UP_SYNC to UPLOADED),
        createCommonUpSyncTags(uniqueMasterSyncId, uniqueSyncId) + listOf(tagForType(UPLOADER)),
        workDataOf(EventUpSyncUploaderWorker.PROGRESS_UP_SYNC to UPLOADED),
        id
    )

private fun createUpSyncCounterWorker(state: WorkInfo.State,
                                      uniqueMasterSyncId: String?,
                                      uniqueSyncId: String? = UNIQUE_UP_SYNC_ID,
                                      id: UUID = UUID.randomUUID()) =
    createWorkInfo(
        state,
        workDataOf(EventUpSyncCountWorker.OUTPUT_COUNT_WORKER_UP to JsonHelper().toJson(EventCount(ENROLMENT_RECORD_CREATION, TO_UPLOAD))),
        createCommonUpSyncTags(uniqueMasterSyncId, uniqueSyncId) + listOf(tagForType(UP_COUNTER)),
        workDataOf(),
        id
    )

fun createCommonDownSyncTags(uniqueMasterSyncId: String?,
                             uniqueSyncId: String?) = listOf(
    "${TAG_DOWN_MASTER_SYNC_ID}${uniqueSyncId}",
    "${TAG_SCHEDULED_AT}${Date().time}",
    TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS,
    TAG_SUBJECTS_SYNC_ALL_WORKERS,
    "${TAG_MASTER_SYNC_ID}${uniqueMasterSyncId}"
)

private fun createCommonUpSyncTags(uniqueMasterSyncId: String?,
                                   uniqueSyncId: String?) = listOf(
    "${TAG_UP_MASTER_SYNC_ID}${uniqueSyncId}",
    "${TAG_SCHEDULED_AT}${Date().time}",
    TAG_SUBJECTS_UP_SYNC_ALL_WORKERS,
    TAG_SUBJECTS_SYNC_ALL_WORKERS,
    "${TAG_MASTER_SYNC_ID}${uniqueMasterSyncId}"
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
