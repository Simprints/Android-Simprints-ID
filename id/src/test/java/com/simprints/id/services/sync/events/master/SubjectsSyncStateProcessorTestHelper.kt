package com.simprints.id.services.sync.events.master

import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.*
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.eventsystem.event.domain.EventCount
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_RECORD_CREATION
import com.simprints.id.services.sync.events.common.*
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker.Companion.OUTPUT_COUNT_WORKER_DOWN
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImplTest.Companion.DOWNLOADED
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImplTest.Companion.TO_DOWNLOAD
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImplTest.Companion.TO_UPLOAD
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImplTest.Companion.UNIQUE_DOWN_SYNC_ID
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImplTest.Companion.UNIQUE_SYNC_ID
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImplTest.Companion.UNIQUE_UP_SYNC_ID
import com.simprints.id.services.sync.events.master.EventSyncStateProcessorImplTest.Companion.UPLOADED
import com.simprints.id.services.sync.events.master.internal.OUTPUT_ESTIMATED_MAINTENANCE_TIME
import com.simprints.id.services.sync.events.master.internal.OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE
import com.simprints.id.services.sync.events.master.internal.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.id.services.sync.events.master.internal.OUTPUT_FAILED_BECAUSE_TOO_MANY_REQUESTS
import com.simprints.eventsystem.events_sync.models.EventSyncState
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerState
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerState.*
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerType.*
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.id.services.sync.events.up.workers.EventUpSyncCountWorker.Companion.OUTPUT_COUNT_WORKER_UP
import com.simprints.id.services.sync.events.up.workers.EventUpSyncUploaderWorker.Companion.OUTPUT_UP_SYNC
import com.simprints.id.services.sync.events.up.workers.EventUpSyncUploaderWorker.Companion.PROGRESS_UP_SYNC
import java.util.*

fun EventSyncWorkerState.assertEqualToFailedState(e: Failed) {
    assertThat(this).isInstanceOf(Failed::class.java)
    val failed = this as Failed
    assertThat(failed.estimatedOutage).isEqualTo(e.estimatedOutage)
    assertThat(failed.failedBecauseCloudIntegration).isEqualTo(e.failedBecauseCloudIntegration)
    assertThat(failed.failedBecauseBackendMaintenance).isEqualTo(e.failedBecauseBackendMaintenance)
    assertThat(failed.failedBecauseTooManyRequest).isEqualTo(e.failedBecauseTooManyRequest)
}

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

fun createWorkInfosHistoryForFailingSyncDueBackendMaintenanceError(): List<WorkInfo> =
    listOf(
        createDownSyncDownloaderWorker(
            FAILED, UNIQUE_SYNC_ID, workDataOf(
                OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                OUTPUT_ESTIMATED_MAINTENANCE_TIME to 6L
            )
        ),
        createDownSyncCounterWorker(SUCCEEDED, UNIQUE_SYNC_ID),
        createUpSyncUploaderWorker(SUCCEEDED, UNIQUE_SYNC_ID),
        createUpSyncCounterWorker(SUCCEEDED, UNIQUE_SYNC_ID)
    )

fun createWorkInfosHistoryForFailingSyncDueTooManyRequestsError(): List<WorkInfo> =
    listOf(
        createDownSyncDownloaderWorker(
            FAILED, UNIQUE_SYNC_ID, workDataOf(
                OUTPUT_FAILED_BECAUSE_TOO_MANY_REQUESTS to true,
            )
        ),
        createDownSyncCounterWorker(SUCCEEDED, UNIQUE_SYNC_ID),
        createUpSyncUploaderWorker(SUCCEEDED, UNIQUE_SYNC_ID),
        createUpSyncCounterWorker(SUCCEEDED, UNIQUE_SYNC_ID)
    )

fun createWorkInfosHistoryForFailingSyncDueCloudIntegrationError(): List<WorkInfo> =
    listOf(
        createDownSyncDownloaderWorker(
            FAILED, UNIQUE_SYNC_ID, workDataOf(
                OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true,
            )
        ),
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

private fun createDownSyncDownloaderWorker(
    state: WorkInfo.State,
    uniqueMasterSyncId: String?,
    tag: Data = workDataOf(),
    uniqueSyncId: String? = UNIQUE_DOWN_SYNC_ID,
    id: UUID = UUID.randomUUID()
) =
    createWorkInfo(
        state,
        concatData(workDataOf(OUTPUT_DOWN_SYNC to DOWNLOADED), tag),
        createCommonDownSyncTags(uniqueMasterSyncId, uniqueSyncId) + listOf(tagForType(DOWNLOADER)),
        workDataOf(PROGRESS_DOWN_SYNC to DOWNLOADED),
        id
    )

private fun createDownSyncCounterWorker(
    state: WorkInfo.State,
    uniqueMasterSyncId: String?,
    uniqueSyncId: String? = UNIQUE_DOWN_SYNC_ID,
    id: UUID = UUID.randomUUID()
) =
    createWorkInfo(
        state,
        workDataOf(
            OUTPUT_COUNT_WORKER_DOWN to JsonHelper.toJson(
                listOf(
                    EventCount(
                        ENROLMENT_RECORD_CREATION,
                        TO_DOWNLOAD
                    )
                )
            )
        ),
        createCommonDownSyncTags(
            uniqueMasterSyncId,
            uniqueSyncId
        ) + listOf(tagForType(DOWN_COUNTER)),
        workDataOf(),
        id
    )

private fun createUpSyncUploaderWorker(
    state: WorkInfo.State,
    uniqueMasterSyncId: String?,
    uniqueSyncId: String? = UNIQUE_UP_SYNC_ID,
    id: UUID = UUID.randomUUID()
) =
    createWorkInfo(
        state,
        workDataOf(OUTPUT_UP_SYNC to UPLOADED),
        createCommonUpSyncTags(uniqueMasterSyncId, uniqueSyncId) + listOf(tagForType(UPLOADER)),
        workDataOf(PROGRESS_UP_SYNC to UPLOADED),
        id
    )

private fun createUpSyncCounterWorker(
    state: WorkInfo.State,
    uniqueMasterSyncId: String?,
    uniqueSyncId: String? = UNIQUE_UP_SYNC_ID,
    id: UUID = UUID.randomUUID()
) =
    createWorkInfo(
        state,
        workDataOf(OUTPUT_COUNT_WORKER_UP to TO_UPLOAD),
        createCommonUpSyncTags(uniqueMasterSyncId, uniqueSyncId) + listOf(tagForType(UP_COUNTER)),
        workDataOf(),
        id
    )

fun createCommonDownSyncTags(
    uniqueMasterSyncId: String?,
    uniqueSyncId: String?
) = listOf(
    "${TAG_DOWN_MASTER_SYNC_ID}${uniqueSyncId}",
    "${TAG_SCHEDULED_AT}${Date().time}",
    TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS,
    TAG_SUBJECTS_SYNC_ALL_WORKERS,
    "${TAG_MASTER_SYNC_ID}${uniqueMasterSyncId}"
)

private fun createCommonUpSyncTags(
    uniqueMasterSyncId: String?,
    uniqueSyncId: String?
) = listOf(
    "${TAG_UP_MASTER_SYNC_ID}${uniqueSyncId}",
    "${TAG_SCHEDULED_AT}${Date().time}",
    TAG_SUBJECTS_UP_SYNC_ALL_WORKERS,
    TAG_SUBJECTS_SYNC_ALL_WORKERS,
    "${TAG_MASTER_SYNC_ID}${uniqueMasterSyncId}"
)

fun createWorkInfo(
    state: WorkInfo.State,
    output: Data = workDataOf(),
    tags: List<String> = emptyList(),
    progress: Data = workDataOf(),
    id: UUID = UUID.randomUUID()
) =
    WorkInfo(
        id,
        state,
        output,
        tags,
        progress,
        0
    )

private fun concatData(d1: Data, d2: Data): Data {
    val dataBuilder = Data.Builder()
    d1.keyValueMap.forEach { dataBuilder.put(it.key, it.value) }
    d2.keyValueMap.forEach { dataBuilder.put(it.key, it.value) }
    return dataBuilder.build()
}
