package com.simprints.feature.validatepool.usecase

import com.simprints.infra.sync.SyncCommand
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
        val lastSyncId = sync(eventSync = SyncCommand.ObserveOnly, imageSync = SyncCommand.ObserveOnly)
            .map { it.eventSyncState }
            .firstOrNull { !it.isUninitialized() }
            ?.syncId

        syncOrchestrator.startEventSync()
        sync(eventSync = SyncCommand.ObserveOnly, imageSync = SyncCommand.ObserveOnly)
            .map { it.eventSyncState }
            .firstOrNull { it.syncId != lastSyncId && it.isSyncReporterCompleted() }
    }
}
