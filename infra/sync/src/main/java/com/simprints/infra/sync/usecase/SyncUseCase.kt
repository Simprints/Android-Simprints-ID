package com.simprints.infra.sync.usecase

import com.simprints.core.AppScope
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.sync.EventSyncStateProcessor
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncCommand
import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.SyncResponse
import com.simprints.infra.sync.SyncStatus
import com.simprints.infra.sync.usecase.internal.ExecuteSyncCommandUseCase
import com.simprints.infra.sync.usecase.internal.ObserveImageSyncStatusUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Combines statuses of syncable entities together in a reactive way.
 *
 * Because sync state is extensively used throughout the project, including synchronously,
 * it is an app-scoped StateFlow. An up-to-date sync state value can be accessed synchronously.
 */
@Singleton
class SyncUseCase @Inject internal constructor(
    eventSyncStateProcessor: EventSyncStateProcessor,
    imageSync: ObserveImageSyncStatusUseCase,
    private val executeSyncCommand: ExecuteSyncCommandUseCase,
    @param:AppScope private val appScope: CoroutineScope,
) {
    private val defaultEventSyncState = EventSyncState(
        syncId = "",
        progress = null,
        total = null,
        upSyncWorkersInfo = emptyList(),
        downSyncWorkersInfo = emptyList(),
        reporterStates = emptyList(),
        lastSyncTime = null,
    )
    private val defaultImageSyncStatus = ImageSyncStatus(
        isSyncing = false,
        progress = null,
        lastUpdateTimeMillis = -1L,
    )
    private val defaultSyncStatus = SyncStatus(defaultEventSyncState, defaultImageSyncStatus)

    private val sharedSyncStatus: StateFlow<SyncStatus> by lazy {
        combine(
            eventSyncStateProcessor.getLastSyncState().onStart { emit(defaultEventSyncState) },
            imageSync().onStart { emit(defaultImageSyncStatus) },
        ) { eventSyncState, imageSyncStatus ->
            SyncStatus(eventSyncState, imageSyncStatus)
        }.stateIn(
            appScope,
            SharingStarted.Eagerly,
            defaultSyncStatus,
        )
    }

    /**
     * Takes sync control commands (incl. no action) for syncable entities.
     * Returns the command progress job and the syncable entities' combined sync status,
     * with a .value also available to the callers synchronously.
     *
     * Usage:
     * sync(
     *  SyncCommands.
     *   +- ObserveOnly.
     *   +- Schedule.
     *   |   +- Everything. --->|
     *   |   +- Events.     --->|           |--->   stop()
     *   |   +- Images.     --->|    for    |--->   start()
     *   +- OneTime.            |---------->|--->   stopAndStart()
     *       +- Events.     --->|    all    |--->   stopAndStartAround { /* stop, run this block, then start */ }
     *       +- Images.     --->|
     * )
     *
     * Examples:
     *
     *  sync(SyncCommands.ObserveOnly)
     *  sync(SyncCommands.OneTime.Events.stop())
     *  sync(SyncCommands.OneTime.Images.stopAndStart())  // starts even if wasn't running at stop command time
     *  sync(SyncCommands.Schedule.Events.start())
     *  sync(SyncCommands.Schedule.Everything.stopAndStartAround {
     *      delay(10_000)   // transaction to wait for...
     *  }).await()          // ...now complete
     *  val lastEventSyncTime = sync(SyncCommands.ObserveOnly).syncStatusFlow.value.eventSyncState.lastSyncTime
     *
     * Sync commands intentionally do not have default values,
     * to prevent a `sync()` usage from being interpreted as a command to start syncing.
     *
     * Sync returns a combo of a Job for the command and the flow of sync statuses.
     * For non-blocking use, the job doesn't matter.
     * If the command was for a inherently non-blocking job, it will be returned already completed.
     * To suspend until the command completes, add .await(), it rethrows cancellations / other exceptions.
     *
     * The commandScope param allows the sync command (incl. the optional stopAndStartAround block)
     * be cancelable when the passed scope's coroutine is cancelled,
     * and to allow stopAndStartAround throw exceptions in the passed scopes coroutine's context.
     * Note: cancelling a command may leave the corresponding sync in a stopped state. The stopping is synchronous.
     */
    operator fun invoke(
        syncCommand: SyncCommand,
        commandScope: CoroutineScope = appScope,
    ): SyncResponse = SyncResponse(
        syncCommandJob = when (syncCommand) {
            is SyncCommands.ExecutableSyncCommand -> executeSyncCommand(syncCommand, commandScope)
            is SyncCommands.ObserveOnly -> Job().apply { complete() } // no-op
        },
        syncStatusFlow = sharedSyncStatus,
    )
}
