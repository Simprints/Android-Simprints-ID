package com.simprints.feature.validatepool.usecase

import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.usecase.SyncUseCase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class RunBlockingEventSyncUseCase @Inject constructor(
    private val sync: SyncUseCase,
    private val syncOrchestrator: SyncOrchestrator,
) {
    suspend operator fun invoke() {
        // First item in the flow (except uninitialized) is the state of last sync,
        // so it can be used to as a filter out old sync states
        val lastSyncId = sync(SyncCommands.ObserveOnly).syncStatusFlow
            .map { it.eventSyncState }
            .firstOrNull { !it.isUninitialized() }
            ?.syncId

        syncOrchestrator.startEventSync()
        sync(SyncCommands.ObserveOnly).syncStatusFlow
            .map { it.eventSyncState }
            .firstOrNull { it.syncId != lastSyncId && it.isSyncReporterCompleted() }
    }
}
