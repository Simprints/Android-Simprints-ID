package com.simprints.infra.sync

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.imagesUploadRequiresUnmeteredConnection
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.sync.master.EventSyncMasterWorker
import com.simprints.infra.sync.extensions.schedulePeriodicWorker
import com.simprints.infra.sync.extensions.startWorker
import com.simprints.infra.sync.config.worker.DeviceConfigDownSyncWorker
import com.simprints.infra.sync.config.worker.ProjectConfigDownSyncWorker
import com.simprints.infra.sync.enrolments.EnrolmentRecordWorker
import com.simprints.infra.sync.extensions.cancelWorkers
import com.simprints.infra.sync.firmware.FirmwareFileUpdateWorker
import com.simprints.infra.sync.usecase.CleanupDeprecatedWorkersUseCase
import com.simprints.infra.sync.images.ImageUpSyncWorker
import com.simprints.infra.sync.firmware.ShouldScheduleFirmwareUpdateUseCase
import javax.inject.Inject

internal class SyncOrchestratorImpl @Inject constructor(
    private val workManager: WorkManager,
    private val authStore: AuthStore,
    private val configRepo: ConfigRepository,
    private val eventSyncManager: EventSyncManager,
    private val shouldScheduleFirmwareUpdate: ShouldScheduleFirmwareUpdateUseCase,
    private val cleanupDeprecatedWorkers: CleanupDeprecatedWorkersUseCase,
) : SyncOrchestrator {

    override suspend fun scheduleBackgroundWork() {
        if (authStore.signedInProjectId.isNotEmpty()) {
            workManager.schedulePeriodicWorker<ProjectConfigDownSyncWorker>(
                SyncConstants.PROJECT_SYNC_WORK_NAME,
                SyncConstants.PROJECT_SYNC_REPEAT_INTERVAL
            )
            workManager.schedulePeriodicWorker<DeviceConfigDownSyncWorker>(
                SyncConstants.DEVICE_SYNC_WORK_NAME,
                SyncConstants.DEVICE_SYNC_REPEAT_INTERVAL
            )
            workManager.schedulePeriodicWorker<ImageUpSyncWorker>(
                SyncConstants.IMAGE_UP_SYNC_WORK_NAME,
                SyncConstants.IMAGE_UP_SYNC_REPEAT_INTERVAL,
                constraints = getImageUploadConstraints()
            )
            rescheduleEventSync()
            if (shouldScheduleFirmwareUpdate()) {
                workManager.schedulePeriodicWorker<FirmwareFileUpdateWorker>(
                    SyncConstants.FIRMWARE_UPDATE_WORK_NAME,
                    SyncConstants.FIRMWARE_UPDATE_REPEAT_INTERVAL,
                )
            } else {
                workManager.cancelWorkers(SyncConstants.FIRMWARE_UPDATE_WORK_NAME)
            }
        }
    }

    override suspend fun cancelBackgroundWork() {
        workManager.cancelWorkers(
            SyncConstants.PROJECT_SYNC_WORK_NAME,
            SyncConstants.DEVICE_SYNC_WORK_NAME,
            SyncConstants.IMAGE_UP_SYNC_WORK_NAME,
            SyncConstants.EVENT_SYNC_WORK_NAME,
            SyncConstants.FIRMWARE_UPDATE_WORK_NAME,
        )
        stopEventSync()
    }

    override fun startDeviceSync() {
        workManager.startWorker<DeviceConfigDownSyncWorker>(SyncConstants.DEVICE_SYNC_WORK_NAME_ONE_TIME)
    }

    override fun rescheduleEventSync() {
        workManager.schedulePeriodicWorker<EventSyncMasterWorker>(
            SyncConstants.EVENT_SYNC_WORK_NAME,
            SyncConstants.EVENT_SYNC_WORKER_INTERVAL,
            tags = eventSyncManager.getPeriodicWorkTags(),
        )
    }

    override fun cancelEventSync() {
        workManager.cancelWorkers(SyncConstants.EVENT_SYNC_WORK_NAME)
        stopEventSync()
    }

    override fun startEventSync() {
        workManager.startWorker<EventSyncMasterWorker>(
            SyncConstants.EVENT_SYNC_WORK_NAME_ONE_TIME,
            tags = eventSyncManager.getOneTimeWorkTags(),
        )
    }

    override fun stopEventSync() {
        workManager.cancelWorkers(SyncConstants.EVENT_SYNC_WORK_NAME_ONE_TIME)
        // Event sync consists of multiple workers, so we cancel them all by tag
        workManager.cancelAllWorkByTag(eventSyncManager.getAllWorkerTag())
    }

    override suspend fun rescheduleImageUpSync() {
        workManager.schedulePeriodicWorker<ImageUpSyncWorker>(
            SyncConstants.IMAGE_UP_SYNC_WORK_NAME,
            SyncConstants.IMAGE_UP_SYNC_REPEAT_INTERVAL,
            existingWorkPolicy = ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            constraints = getImageUploadConstraints()
        )
    }

    override fun uploadEnrolmentRecords(id: String, subjectIds: List<String>) {
        workManager.startWorker<EnrolmentRecordWorker>(
            SyncConstants.RECORD_UPLOAD_WORK_NAME,
            inputData = workDataOf(
                SyncConstants.RECORD_UPLOAD_INPUT_ID_NAME to id,
                SyncConstants.RECORD_UPLOAD_INPUT_SUBJECT_IDS_NAME to subjectIds.toTypedArray()
            ),
        )
    }

    override suspend fun deleteEventSyncInfo() {
        eventSyncManager.deleteSyncInfo()
        workManager.pruneWork()
    }

    override fun cleanupWorkers() {
        cleanupDeprecatedWorkers()
    }


    private suspend fun getImageUploadConstraints(): Constraints {
        val networkType = configRepo
            .getProjectConfiguration()
            .imagesUploadRequiresUnmeteredConnection()
            .let { if (it) NetworkType.UNMETERED else NetworkType.CONNECTED }
        return Constraints.Builder().setRequiredNetworkType(networkType).build()
    }
}
