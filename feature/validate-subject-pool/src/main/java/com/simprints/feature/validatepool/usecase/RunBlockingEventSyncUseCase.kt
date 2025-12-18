package com.simprints.feature.validatepool.usecase

import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.sync.SyncOrchestrator
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

internal class RunBlockingEventSyncUseCase @Inject constructor(
    private val syncManager: EventSyncManager,
    private val syncOrchestrator: SyncOrchestrator,
) {
    suspend operator fun invoke() {
        // First item in the flow is the state of last sync,
        // so it can be used to as a filter out old sync states
        val lastSyncId = syncManager
            .getLastSyncState()
            .firstOrNull()
            ?.syncId

        syncOrchestrator.startEventSync()
        syncManager
            .getLastSyncState()
            .firstOrNull { it.syncId != lastSyncId && it.isSyncReporterCompleted() }
    }
}
