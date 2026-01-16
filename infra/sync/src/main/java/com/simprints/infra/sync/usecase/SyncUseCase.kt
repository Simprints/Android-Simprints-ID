package com.simprints.infra.sync.usecase

import com.simprints.core.AppScope
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncCommand
import com.simprints.infra.sync.SyncStatus
import com.simprints.infra.sync.usecase.internal.EventSyncUseCase
import com.simprints.infra.sync.usecase.internal.ImageSyncUseCase
import kotlinx.coroutines.CoroutineScope
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
class SyncUseCase @Inject constructor(
    eventSync: EventSyncUseCase,
    imageSync: ImageSyncUseCase,
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
            eventSync().onStart { emit(defaultEventSyncState) },
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
     * Takes sync control commands (incl. no action) for syncable entities, and returns their combined sync status,
     * with a .value also available to the callers synchronously.
     *
     * Sync commands intentionally do not have default values,
     * to prevent a `sync()` usage from being interpreted as a command to start syncing.
     */
    operator fun invoke(
        eventSync: SyncCommand,
        imageSync: SyncCommand,
    ): StateFlow<SyncStatus> = sharedSyncStatus
    // todo MS-1278 move sync commands here from SyncOrchestrator (use helper usecases if needed), add to SyncCommand, and implement them
}
