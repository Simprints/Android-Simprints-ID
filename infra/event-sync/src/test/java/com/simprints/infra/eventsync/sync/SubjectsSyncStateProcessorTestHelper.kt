package com.simprints.infra.eventsync.sync

import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.ENQUEUED
import androidx.work.WorkInfo.State.FAILED
import androidx.work.WorkInfo.State.RUNNING
import androidx.work.WorkInfo.State.SUCCEEDED
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState.Enqueued
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState.Failed
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState.Running
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState.Succeeded
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.DOWNLOADER
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.UPLOADER
import com.simprints.infra.eventsync.sync.EventSyncStateProcessorTest.Companion.DOWNLOADED
import com.simprints.infra.eventsync.sync.EventSyncStateProcessorTest.Companion.TO_DOWNLOAD
import com.simprints.infra.eventsync.sync.EventSyncStateProcessorTest.Companion.TO_UPLOAD
import com.simprints.infra.eventsync.sync.EventSyncStateProcessorTest.Companion.UNIQUE_DOWN_SYNC_ID
import com.simprints.infra.eventsync.sync.EventSyncStateProcessorTest.Companion.UNIQUE_SYNC_ID
import com.simprints.infra.eventsync.sync.EventSyncStateProcessorTest.Companion.UNIQUE_UP_SYNC_ID
import com.simprints.infra.eventsync.sync.EventSyncStateProcessorTest.Companion.UPLOADED
import com.simprints.infra.eventsync.sync.common.OUTPUT_ESTIMATED_MAINTENANCE_TIME
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_TOO_MANY_REQUESTS
import com.simprints.infra.eventsync.sync.common.TAG_DOWN_MASTER_SYNC_ID
import com.simprints.infra.eventsync.sync.common.TAG_MASTER_SYNC_ID
import com.simprints.infra.eventsync.sync.common.TAG_SCHEDULED_AT
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_UP_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.common.TAG_UP_MASTER_SYNC_ID
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_MAX_SYNC
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_MAX_SYNC
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker.Companion.OUTPUT_UP_MAX_SYNC
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker.Companion.OUTPUT_UP_SYNC
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker.Companion.PROGRESS_UP_SYNC
import java.util.Date
import java.util.UUID

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

fun EventSyncState.assertRunningSyncStateWithoutProgress() {
    assertThat(syncId).isEqualTo(UNIQUE_SYNC_ID)
    assertThat(total).isNull()
    assertThat(progress).isNull()
    assertThat(downSyncWorkersInfo.count { it.state is Running }).isEqualTo(1)
    upSyncWorkersInfo.all { it.state is Succeeded }
}

private fun assertProgressAndTotal(
    syncId: String,
    total: Int?,
    progress: Int?,
) {
    assertThat(syncId).isEqualTo(UNIQUE_SYNC_ID)
    assertThat(total).isEqualTo(TO_DOWNLOAD + TO_UPLOAD)
    assertThat(progress).isEqualTo(DOWNLOADED + UPLOADED)
}

fun createWorkInfosHistoryForSuccessfulSync(): List<WorkInfo> = listOf(
    createDownSyncDownloaderWorker(SUCCEEDED, UNIQUE_SYNC_ID),
    createUpSyncUploaderWorker(SUCCEEDED, UNIQUE_SYNC_ID),
)

fun createWorkInfosHistoryForRunningSync(): List<WorkInfo> = listOf(
    createDownSyncDownloaderWorker(RUNNING, UNIQUE_SYNC_ID),
    createUpSyncUploaderWorker(SUCCEEDED, UNIQUE_SYNC_ID),
)

fun createWorkInfosHistoryForFailingSync(): List<WorkInfo> = listOf(
    createDownSyncDownloaderWorker(FAILED, UNIQUE_SYNC_ID),
    createUpSyncUploaderWorker(SUCCEEDED, UNIQUE_SYNC_ID),
)

fun createWorkInfosHistoryForFailingSyncDueBackendMaintenanceError(): List<WorkInfo> = listOf(
    createDownSyncDownloaderWorker(
        FAILED,
        UNIQUE_SYNC_ID,
        workDataOf(
            OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
            OUTPUT_ESTIMATED_MAINTENANCE_TIME to 6L,
        ),
    ),
    createUpSyncUploaderWorker(SUCCEEDED, UNIQUE_SYNC_ID),
)

fun createWorkInfosHistoryForFailingSyncDueTooManyRequestsError(): List<WorkInfo> = listOf(
    createDownSyncDownloaderWorker(
        FAILED,
        UNIQUE_SYNC_ID,
        workDataOf(
            OUTPUT_FAILED_BECAUSE_TOO_MANY_REQUESTS to true,
        ),
    ),
    createUpSyncUploaderWorker(SUCCEEDED, UNIQUE_SYNC_ID),
)

fun createWorkInfosHistoryForFailingSyncDueCloudIntegrationError(): List<WorkInfo> = listOf(
    createDownSyncDownloaderWorker(
        FAILED,
        UNIQUE_SYNC_ID,
        workDataOf(
            OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true,
        ),
    ),
    createUpSyncUploaderWorker(SUCCEEDED, UNIQUE_SYNC_ID),
)

fun createWorkInfosHistoryForConnectingSync(): List<WorkInfo> = listOf(
    createDownSyncDownloaderWorker(ENQUEUED, UNIQUE_SYNC_ID),
    createUpSyncUploaderWorker(SUCCEEDED, UNIQUE_SYNC_ID),
)

private fun createDownSyncDownloaderWorker(
    state: WorkInfo.State,
    uniqueMasterSyncId: String?,
    tag: Data = workDataOf(),
    uniqueSyncId: String? = UNIQUE_DOWN_SYNC_ID,
    id: UUID = UUID.randomUUID(),
) = createWorkInfo(
    state,
    concatData(
        workDataOf(
            OUTPUT_DOWN_SYNC to DOWNLOADED,
            OUTPUT_DOWN_MAX_SYNC to TO_DOWNLOAD,
        ),
        tag,
    ),
    createCommonDownSyncTags(uniqueMasterSyncId, uniqueSyncId) + listOf(tagForType(DOWNLOADER)),
    workDataOf(
        PROGRESS_DOWN_SYNC to DOWNLOADED,
        PROGRESS_DOWN_MAX_SYNC to TO_DOWNLOAD,
    ),
    id,
)

private fun createUpSyncUploaderWorker(
    state: WorkInfo.State,
    uniqueMasterSyncId: String?,
    uniqueSyncId: String? = UNIQUE_UP_SYNC_ID,
    id: UUID = UUID.randomUUID(),
) = createWorkInfo(
    state,
    workDataOf(
        OUTPUT_UP_SYNC to UPLOADED,
        OUTPUT_UP_MAX_SYNC to TO_UPLOAD,
    ),
    createCommonUpSyncTags(uniqueMasterSyncId, uniqueSyncId) + setOf(tagForType(UPLOADER)),
    workDataOf(
        PROGRESS_UP_SYNC to UPLOADED,
        PROGRESS_UP_SYNC to TO_UPLOAD,
    ),
    id,
)

fun createCommonDownSyncTags(
    uniqueMasterSyncId: String?,
    uniqueSyncId: String?,
) = setOf(
    "$TAG_DOWN_MASTER_SYNC_ID$uniqueSyncId",
    "$TAG_SCHEDULED_AT${Date().time}",
    TAG_SUBJECTS_DOWN_SYNC_ALL_WORKERS,
    TAG_SUBJECTS_SYNC_ALL_WORKERS,
    "$TAG_MASTER_SYNC_ID$uniqueMasterSyncId",
)

private fun createCommonUpSyncTags(
    uniqueMasterSyncId: String?,
    uniqueSyncId: String?,
) = setOf(
    "$TAG_UP_MASTER_SYNC_ID$uniqueSyncId",
    "$TAG_SCHEDULED_AT${Date().time}",
    TAG_SUBJECTS_UP_SYNC_ALL_WORKERS,
    TAG_SUBJECTS_SYNC_ALL_WORKERS,
    "$TAG_MASTER_SYNC_ID$uniqueMasterSyncId",
)

fun createWorkInfo(
    state: WorkInfo.State,
    output: Data = workDataOf(),
    tags: Set<String> = emptySet(),
    progress: Data = workDataOf(),
    id: UUID = UUID.randomUUID(),
) = WorkInfo(
    id,
    state,
    tags,
    output,
    progress,
    0,
    0,
)

private fun concatData(
    d1: Data,
    d2: Data,
): Data {
    val dataBuilder = Data.Builder()
    d1.keyValueMap.forEach { dataBuilder.put(it.key, it.value) }
    d2.keyValueMap.forEach { dataBuilder.put(it.key, it.value) }
    return dataBuilder.build()
}
