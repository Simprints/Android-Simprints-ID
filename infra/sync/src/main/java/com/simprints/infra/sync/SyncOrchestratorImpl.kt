package com.simprints.infra.sync

import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.workDataOf
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.sync.config.worker.DeviceConfigDownSyncWorker
import com.simprints.infra.sync.config.worker.ProjectConfigDownSyncWorker
import com.simprints.infra.sync.enrolments.EnrolmentRecordWorker
import com.simprints.infra.sync.extensions.startWorker
import com.simprints.infra.sync.usecase.CleanupDeprecatedWorkersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SyncOrchestratorImpl @Inject constructor(
    private val workManager: WorkManager,
    private val eventSyncManager: EventSyncManager,
    private val cleanupDeprecatedWorkers: CleanupDeprecatedWorkersUseCase,
    private val imageSyncTimestampProvider: ImageSyncTimestampProvider,
) : SyncOrchestrator {
    override fun startConfigSync() {
        workManager.startWorker<ProjectConfigDownSyncWorker>(SyncConstants.PROJECT_SYNC_WORK_NAME_ONE_TIME)
        workManager.startWorker<DeviceConfigDownSyncWorker>(SyncConstants.DEVICE_SYNC_WORK_NAME_ONE_TIME)
    }

    override fun refreshConfiguration(): Flow<Unit> {
        startConfigSync()
        return workManager
            .getWorkInfosFlow(
                WorkQuery.fromUniqueWorkNames(
                    SyncConstants.PROJECT_SYNC_WORK_NAME_ONE_TIME,
                    SyncConstants.DEVICE_SYNC_WORK_NAME_ONE_TIME,
                ),
            ).filter { workInfoList ->
                workInfoList.none { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
            }.map { } // Converts flow emissions to Unit value as we only care about when it happens, not the value
    }

    override fun uploadEnrolmentRecords(
        id: String,
        subjectIds: List<String>,
    ) {
        workManager.startWorker<EnrolmentRecordWorker>(
            SyncConstants.RECORD_UPLOAD_WORK_NAME,
            inputData = workDataOf(
                SyncConstants.RECORD_UPLOAD_INPUT_ID_NAME to id,
                SyncConstants.RECORD_UPLOAD_INPUT_SUBJECT_IDS_NAME to subjectIds.toTypedArray(),
            ),
        )
    }

    override suspend fun deleteEventSyncInfo() {
        eventSyncManager.deleteSyncInfo()
        workManager.pruneWork()
        imageSyncTimestampProvider.clearTimestamp()
    }

    override fun cleanupWorkers() {
        cleanupDeprecatedWorkers()
    }
}
