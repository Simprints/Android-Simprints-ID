package com.simprints.infra.sync.usecase

import com.simprints.core.AppScope
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.LegacySyncStates
import com.simprints.infra.sync.SyncCommand
import com.simprints.infra.sync.SyncStatus
import com.simprints.infra.sync.usecase.internal.EventSyncUseCase
import com.simprints.infra.sync.usecase.internal.ImageSyncUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncUseCase @Inject constructor(
    eventSync: EventSyncUseCase,
    imageSync: ImageSyncUseCase,
    @param:AppScope private val appScope: CoroutineScope,
) {
    private val defaultEvent = EventSyncState(
        syncId = "",
        progress = null,
        total = null,
        upSyncWorkersInfo = emptyList(),
        downSyncWorkersInfo = emptyList(),
        reporterStates = emptyList(),
        lastSyncTime = null,
    )
    private val defaultImage = ImageSyncStatus(
        isSyncing = false,
        progress = null,
        lastUpdateTimeMillis = -1L,
    )

    private val sharedSyncStatus: StateFlow<SyncStatus> by lazy {
        combine(
            eventSync(),
            imageSync(),
        ) { eventSyncState, imageSyncStatus ->
            SyncStatus(LegacySyncStates(eventSyncState, imageSyncStatus))
        }.stateIn(
            appScope,
            SharingStarted.Eagerly,
            SyncStatus(LegacySyncStates(defaultEvent, defaultImage)),
        )
    }

    operator fun invoke(eventSync: SyncCommand, imageSync: SyncCommand): StateFlow<SyncStatus> = sharedSyncStatus

}
