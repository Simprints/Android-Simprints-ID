package com.simprints.infra.sync.usecase.internal

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.workDataOf
import com.simprints.core.AppScope
import com.simprints.core.DispatcherIO
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.imagesUploadRequiresUnmeteredConnection
import com.simprints.infra.config.store.models.isCommCareEventDownSyncAllowed
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.sync.master.EventSyncMasterWorker
import com.simprints.infra.sync.SyncAction
import com.simprints.infra.sync.SyncCommandPayload
import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.SyncConstants
import com.simprints.infra.sync.SyncTarget
import com.simprints.infra.sync.config.worker.DeviceConfigDownSyncWorker
import com.simprints.infra.sync.config.worker.ProjectConfigDownSyncWorker
import com.simprints.infra.sync.extensions.anyRunning
import com.simprints.infra.sync.extensions.cancelWorkers
import com.simprints.infra.sync.extensions.schedulePeriodicWorker
import com.simprints.infra.sync.extensions.startWorker
import com.simprints.infra.sync.files.FileUpSyncWorker
import com.simprints.infra.sync.firmware.FirmwareFileUpdateWorker
import com.simprints.infra.sync.firmware.ShouldScheduleFirmwareUpdateUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ExecuteSyncCommandUseCase @Inject constructor(
    private val workManager: WorkManager,
    private val authStore: AuthStore,
    private val configRepository: ConfigRepository,
    private val eventSyncManager: EventSyncManager,
    private val shouldScheduleFirmwareUpdate: ShouldScheduleFirmwareUpdateUseCase,
    @param:AppScope private val appScope: CoroutineScope,
    @param:DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) {
    init {
        appScope.launch(ioDispatcher) {
            // Automatically conditioned sync command:
            // Stop image upload when event sync starts
            workManager
                .getWorkInfosFlow(
                    WorkQuery.fromUniqueWorkNames(
                        SyncConstants.EVENT_SYNC_WORK_NAME,
                        SyncConstants.EVENT_SYNC_WORK_NAME_ONE_TIME,
                    ),
                ).map { workInfoList ->
                    workInfoList.anyRunning()
                }.distinctUntilChanged()
                .filter { it } // only if any running
                .collect {
                    rescheduleImageUpSync()
                }
        }
    }

    internal operator fun invoke(
        syncCommand: SyncCommands.ExecutableSyncCommand,
        commandScope: CoroutineScope = appScope,
    ): Job {
        with(syncCommand) {
            val isStopNeeded = action in listOf(SyncAction.STOP, SyncAction.STOP_AND_START)
            if (isStopNeeded) {
                stop()
            }
            val isStartNeeded = action in listOf(SyncAction.START, SyncAction.STOP_AND_START)
            val isFurtherAsyncActionNeeded = blockToRunWhileStopped != null || isStartNeeded
            return if (isFurtherAsyncActionNeeded) {
                commandScope.launch(ioDispatcher) {
                    blockToRunWhileStopped?.invoke()
                    if (isStartNeeded) {
                        start()
                    }
                }
            } else {
                Job().apply { complete() } // no-op
            }
        }
    }

    private fun SyncCommands.ExecutableSyncCommand.stop() {
        when (target) {
            SyncTarget.SCHEDULE_EVERYTHING -> cancelBackgroundWork()
            SyncTarget.SCHEDULE_EVENTS -> cancelEventSync()
            SyncTarget.SCHEDULE_IMAGES -> stopImageSync() // uses same worker as for OneTimeImages
            SyncTarget.ONE_TIME_EVENTS -> stopEventSync()
            SyncTarget.ONE_TIME_IMAGES -> stopImageSync()
        }
    }

    private suspend fun SyncCommands.ExecutableSyncCommand.start() {
        when (target) {
            SyncTarget.SCHEDULE_EVERYTHING -> scheduleBackgroundWork((payload as SyncCommandPayload.WithDelay).withDelay)
            SyncTarget.SCHEDULE_EVENTS -> rescheduleEventSync((payload as SyncCommandPayload.WithDelay).withDelay)
            SyncTarget.SCHEDULE_IMAGES -> rescheduleImageUpSync()
            SyncTarget.ONE_TIME_EVENTS -> startEventSync((payload as SyncCommandPayload.WithDownSyncAllowed).isDownSyncAllowed)
            SyncTarget.ONE_TIME_IMAGES -> startImageSync()
        }
    }

    private suspend fun scheduleBackgroundWork(withDelay: Boolean) {
        if (authStore.signedInProjectId.isNotEmpty()) {
            workManager.schedulePeriodicWorker<ProjectConfigDownSyncWorker>(
                SyncConstants.PROJECT_SYNC_WORK_NAME,
                SyncConstants.PROJECT_SYNC_REPEAT_INTERVAL,
            )
            workManager.schedulePeriodicWorker<DeviceConfigDownSyncWorker>(
                SyncConstants.DEVICE_SYNC_WORK_NAME,
                SyncConstants.DEVICE_SYNC_REPEAT_INTERVAL,
            )
            workManager.schedulePeriodicWorker<FileUpSyncWorker>(
                SyncConstants.FILE_UP_SYNC_WORK_NAME,
                SyncConstants.FILE_UP_SYNC_REPEAT_INTERVAL,
                constraints = getImageUploadConstraints(),
            )
            rescheduleEventSync(withDelay)
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

    private fun cancelBackgroundWork() {
        workManager.cancelWorkers(
            SyncConstants.PROJECT_SYNC_WORK_NAME,
            SyncConstants.DEVICE_SYNC_WORK_NAME,
            SyncConstants.FILE_UP_SYNC_WORK_NAME,
            SyncConstants.EVENT_SYNC_WORK_NAME,
            SyncConstants.FIRMWARE_UPDATE_WORK_NAME,
        )
        stopEventSync()
    }

    private suspend fun rescheduleEventSync(withDelay: Boolean) {
        workManager.schedulePeriodicWorker<EventSyncMasterWorker>(
            workName = SyncConstants.EVENT_SYNC_WORK_NAME,
            repeatInterval = SyncConstants.EVENT_SYNC_WORKER_INTERVAL,
            initialDelay = if (withDelay) SyncConstants.EVENT_SYNC_WORKER_INTERVAL else 0,
            constraints = getEventSyncConstraints(),
            tags = eventSyncManager.getPeriodicWorkTags(),
        )
    }

    private fun cancelEventSync() {
        workManager.cancelWorkers(SyncConstants.EVENT_SYNC_WORK_NAME)
        stopEventSync()
    }

    private suspend fun startEventSync(isDownSyncAllowed: Boolean) {
        workManager.startWorker<EventSyncMasterWorker>(
            workName = SyncConstants.EVENT_SYNC_WORK_NAME_ONE_TIME,
            constraints = getEventSyncConstraints(),
            tags = eventSyncManager.getOneTimeWorkTags(),
            inputData = workDataOf(EventSyncMasterWorker.IS_DOWN_SYNC_ALLOWED to isDownSyncAllowed),
        )
    }

    private fun stopEventSync() {
        workManager.cancelWorkers(SyncConstants.EVENT_SYNC_WORK_NAME_ONE_TIME)
        // Event sync consists of multiple workers, so we cancel them all by tag
        workManager.cancelAllWorkByTag(eventSyncManager.getAllWorkerTag())
    }

    private fun startImageSync() {
        stopImageSync()
        workManager.startWorker<FileUpSyncWorker>(SyncConstants.FILE_UP_SYNC_WORK_NAME)
    }

    private fun stopImageSync() {
        workManager.cancelWorkers(SyncConstants.FILE_UP_SYNC_WORK_NAME)
    }

    /**
     * Fully reschedule the background worker.
     * Should be used in when the configuration that affects scheduling has changed.
     */
    private suspend fun rescheduleImageUpSync() {
        workManager.schedulePeriodicWorker<FileUpSyncWorker>(
            SyncConstants.FILE_UP_SYNC_WORK_NAME,
            SyncConstants.FILE_UP_SYNC_REPEAT_INTERVAL,
            initialDelay = SyncConstants.DEFAULT_BACKOFF_INTERVAL_MINUTES,
            existingWorkPolicy = ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            constraints = getImageUploadConstraints(),
        )
    }

    private suspend fun getImageUploadConstraints(): Constraints {
        val networkType = configRepository
            .getProjectConfiguration()
            .imagesUploadRequiresUnmeteredConnection()
            .let { if (it) NetworkType.UNMETERED else NetworkType.CONNECTED }
        return Constraints.Builder().setRequiredNetworkType(networkType).build()
    }

    private suspend fun getEventSyncConstraints(): Constraints {
        // CommCare doesn't require network connection
        val networkType = configRepository
            .getProjectConfiguration()
            .isCommCareEventDownSyncAllowed()
            .let { if (it) NetworkType.NOT_REQUIRED else NetworkType.CONNECTED }
        return Constraints.Builder().setRequiredNetworkType(networkType).build()
    }
}
