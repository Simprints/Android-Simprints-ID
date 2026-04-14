package com.simprints.infra.sync

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.workDataOf
import com.simprints.core.AppScope
import com.simprints.core.DispatcherIO
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.imagesUploadRequiresUnmeteredConnection
import com.simprints.infra.config.store.models.isCommCareEventDownSyncAllowed
import com.simprints.infra.eventsync.DeleteSyncInfoUseCase
import com.simprints.infra.eventsync.EventSyncWorkerTagRepository
import com.simprints.infra.eventsync.status.models.DownSyncState
import com.simprints.infra.eventsync.status.models.UpSyncState
import com.simprints.infra.eventsync.sync.down.DownSyncStateProcessor
import com.simprints.infra.eventsync.sync.master.EventDownSyncMasterWorker
import com.simprints.infra.eventsync.sync.master.EventUpSyncMasterWorker
import com.simprints.infra.eventsync.sync.up.UpSyncStateProcessor
import com.simprints.infra.sync.config.worker.DeviceConfigDownSyncWorker
import com.simprints.infra.sync.config.worker.ProjectConfigDownSyncWorker
import com.simprints.infra.sync.enrolments.EnrolmentRecordWorker
import com.simprints.infra.sync.extensions.anyRunning
import com.simprints.infra.sync.extensions.cancelWorkers
import com.simprints.infra.sync.extensions.schedulePeriodicWorker
import com.simprints.infra.sync.extensions.startWorker
import com.simprints.infra.sync.files.FileUpSyncWorker
import com.simprints.infra.sync.firmware.FirmwareFileUpdateWorker
import com.simprints.infra.sync.firmware.ShouldScheduleFirmwareUpdateUseCase
import com.simprints.infra.sync.usecase.CleanupDeprecatedWorkersUseCase
import com.simprints.infra.sync.usecase.internal.ObserveImageSyncStatusUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SyncOrchestratorImpl @Inject constructor(
    private val workManager: WorkManager,
    private val authStore: AuthStore,
    private val configRepository: ConfigRepository,
    private val deleteSyncInfo: DeleteSyncInfoUseCase,
    private val eventSyncWorkerTagRepository: EventSyncWorkerTagRepository,
    private val upSyncStateProcessor: UpSyncStateProcessor,
    private val downSyncStateProcessor: DownSyncStateProcessor,
    private val observeImageSyncStatus: ObserveImageSyncStatusUseCase,
    private val shouldScheduleFirmwareUpdate: ShouldScheduleFirmwareUpdateUseCase,
    private val cleanupDeprecatedWorkers: CleanupDeprecatedWorkersUseCase,
    private val imageSyncTimestampProvider: ImageSyncTimestampProvider,
    @param:AppScope private val appScope: CoroutineScope,
    @param:DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : SyncOrchestrator {
    private val defaultUpSyncState = UpSyncState(
        syncId = "",
        progress = null,
        total = null,
        workersInfo = emptyList(),
        lastSyncTime = null,
    )
    private val defaultDownSyncState = DownSyncState(
        syncId = "",
        progress = null,
        total = null,
        workersInfo = emptyList(),
        lastSyncTime = null,
    )
    private val defaultImageSyncStatus = ImageSyncStatus(
        isSyncing = false,
        progress = null,
        lastUpdateTimeMillis = -1L,
    )

    private val sharedUpSyncState: StateFlow<UpSyncState> by lazy {
        upSyncStateProcessor
            .getLastUpSyncState()
            .onStart { emit(defaultUpSyncState) }
            .stateIn(appScope, SharingStarted.Eagerly, defaultUpSyncState)
    }

    private val sharedDownSyncState: StateFlow<DownSyncState> by lazy {
        downSyncStateProcessor
            .getLastDownSyncState()
            .onStart { emit(defaultDownSyncState) }
            .stateIn(appScope, SharingStarted.Eagerly, defaultDownSyncState)
    }

    private val sharedSyncState: StateFlow<SyncStatus> by lazy {
        combine(
            sharedUpSyncState,
            sharedDownSyncState,
            observeImageSyncStatus().onStart { emit(defaultImageSyncStatus) },
        ) { upSyncState, downSyncState, imageSyncStatus ->
            SyncStatus(upSyncState, downSyncState, imageSyncStatus)
        }.stateIn(
            appScope,
            SharingStarted.Eagerly,
            SyncStatus(defaultUpSyncState, defaultDownSyncState, defaultImageSyncStatus),
        )
    }

    init {
        // Automatically conditioned scheduling rule:
        // stop image upload when event sync starts.
        appScope.launch(ioDispatcher) {
            workManager
                .getWorkInfosFlow(
                    WorkQuery.fromUniqueWorkNames(
                        SyncConstants.EVENT_UP_SYNC_WORK_NAME,
                        SyncConstants.EVENT_UP_SYNC_WORK_NAME_ONE_TIME,
                        SyncConstants.EVENT_DOWN_SYNC_WORK_NAME,
                        SyncConstants.EVENT_DOWN_SYNC_WORK_NAME_ONE_TIME,
                    ),
                ).map { workInfoList ->
                    workInfoList.anyRunning()
                }.distinctUntilChanged()
                .filter { it } // only when event sync becomes running
                .collect {
                    rescheduleImageUpSync()
                }
        }
    }

    override fun observeSyncState(): StateFlow<SyncStatus> = sharedSyncState

    override fun observeUpSyncState(): StateFlow<UpSyncState> = sharedUpSyncState

    override fun observeDownSyncState(): StateFlow<DownSyncState> = sharedDownSyncState

    override fun execute(command: OneTime): Job = when (command) {
        is OneTime.UpSyncCommand -> executeOneTimeAction(
            action = command.action,
            stop = ::stopUpSync,
            start = { startUpSync() },
        )

        is OneTime.DownSyncCommand -> executeOneTimeAction(
            action = command.action,
            stop = ::stopDownSync,
            start = { startDownSync() },
        )

        is OneTime.ImagesCommand -> executeOneTimeAction(
            action = command.action,
            stop = ::stopImageSync,
            start = { startImageSync() },
        )
    }

    override fun execute(command: ScheduleCommand): Job = when (command) {
        is ScheduleCommand.EverythingCommand -> executeSchedulingAction(
            action = command.action,
            unschedule = ::cancelBackgroundWork,
            reschedule = { scheduleBackgroundWork(withDelay = command.withDelay) },
        )

        is ScheduleCommand.UpSyncCommand -> executeSchedulingAction(
            action = command.action,
            unschedule = ::cancelUpSync,
            reschedule = { rescheduleUpSync(withDelay = command.withDelay) },
        )

        is ScheduleCommand.DownSyncCommand -> executeSchedulingAction(
            action = command.action,
            unschedule = ::cancelDownSync,
            reschedule = { rescheduleDownSync(withDelay = command.withDelay) },
        )

        is ScheduleCommand.ImagesCommand -> executeSchedulingAction(
            action = command.action,
            unschedule = ::stopImageSync,
            reschedule = { rescheduleImageUpSync() },
        )
    }

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
            }.map { }
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
        deleteSyncInfo()
        workManager.pruneWork()
        imageSyncTimestampProvider.clearTimestamp()
    }

    override fun cleanupWorkers() {
        cleanupDeprecatedWorkers()
    }

    private fun executeOneTimeAction(
        action: OneTime.Action,
        stop: () -> Unit,
        start: suspend () -> Unit,
    ): Job = when (action) {
        OneTime.Action.STOP -> {
            stop()
            Job().apply { complete() }
        }
        OneTime.Action.START -> {
            appScope.launch(ioDispatcher) {
                start()
            }
        }
        OneTime.Action.RESTART -> {
            stop()
            appScope.launch(ioDispatcher) {
                start()
            }
        }
    }

    private fun executeSchedulingAction(
        action: ScheduleCommand.Action,
        unschedule: () -> Unit,
        reschedule: suspend () -> Unit,
    ): Job = when (action) {
        ScheduleCommand.Action.UNSCHEDULE -> {
            unschedule()
            Job().apply { complete() }
        }
        ScheduleCommand.Action.RESCHEDULE -> {
            appScope.launch(ioDispatcher) {
                reschedule()
            }
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
            rescheduleUpSync(withDelay = withDelay)
            rescheduleDownSync(withDelay = withDelay)
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
            SyncConstants.EVENT_UP_SYNC_WORK_NAME,
            SyncConstants.EVENT_DOWN_SYNC_WORK_NAME,
            SyncConstants.FIRMWARE_UPDATE_WORK_NAME,
        )
        stopUpSync()
        stopDownSync()
    }

    private suspend fun rescheduleUpSync(withDelay: Boolean) {
        workManager.schedulePeriodicWorker<EventUpSyncMasterWorker>(
            workName = SyncConstants.EVENT_UP_SYNC_WORK_NAME,
            repeatInterval = SyncConstants.EVENT_SYNC_WORKER_INTERVAL,
            initialDelay = if (withDelay) SyncConstants.EVENT_SYNC_WORKER_INTERVAL else 0,
            constraints = getEventSyncConstraints(),
            tags = eventSyncWorkerTagRepository.getUpSyncPeriodicWorkTags(),
        )
    }

    private suspend fun rescheduleDownSync(withDelay: Boolean) {
        workManager.schedulePeriodicWorker<EventDownSyncMasterWorker>(
            workName = SyncConstants.EVENT_DOWN_SYNC_WORK_NAME,
            repeatInterval = SyncConstants.EVENT_SYNC_WORKER_INTERVAL,
            initialDelay = if (withDelay) SyncConstants.EVENT_SYNC_WORKER_INTERVAL else 0,
            constraints = getEventSyncConstraints(),
            tags = eventSyncWorkerTagRepository.getDownSyncPeriodicWorkTags(),
        )
    }

    private fun cancelUpSync() {
        workManager.cancelWorkers(SyncConstants.EVENT_UP_SYNC_WORK_NAME)
        stopUpSync()
    }

    private fun cancelDownSync() {
        workManager.cancelWorkers(SyncConstants.EVENT_DOWN_SYNC_WORK_NAME)
        stopDownSync()
    }

    private suspend fun startUpSync() {
        workManager.startWorker<EventUpSyncMasterWorker>(
            workName = SyncConstants.EVENT_UP_SYNC_WORK_NAME_ONE_TIME,
            constraints = getEventSyncConstraints(),
            tags = eventSyncWorkerTagRepository.getUpSyncOneTimeWorkTags(),
        )
    }

    private suspend fun startDownSync() {
        workManager.startWorker<EventDownSyncMasterWorker>(
            workName = SyncConstants.EVENT_DOWN_SYNC_WORK_NAME_ONE_TIME,
            constraints = getEventSyncConstraints(),
            tags = eventSyncWorkerTagRepository.getDownSyncOneTimeWorkTags(),
        )
    }

    private fun stopUpSync() {
        workManager.cancelWorkers(SyncConstants.EVENT_UP_SYNC_WORK_NAME_ONE_TIME)
        workManager.cancelAllWorkByTag(eventSyncWorkerTagRepository.getUpSyncAllWorkersTag())
    }

    private fun stopDownSync() {
        workManager.cancelWorkers(SyncConstants.EVENT_DOWN_SYNC_WORK_NAME_ONE_TIME)
        workManager.cancelAllWorkByTag(eventSyncWorkerTagRepository.getDownSyncAllWorkersTag())
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
     * Should be used when the configuration that affects scheduling has changed.
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
        // CommCare doesn't require network connection.
        val networkType = configRepository
            .getProjectConfiguration()
            .isCommCareEventDownSyncAllowed()
            .let { if (it) NetworkType.NOT_REQUIRED else NetworkType.CONNECTED }
        return Constraints.Builder().setRequiredNetworkType(networkType).build()
    }
}
