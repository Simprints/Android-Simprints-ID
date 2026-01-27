package com.simprints.feature.validatepool.usecase

import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.usecase.SyncUseCase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class RunBlockingEventSyncUseCase @Inject constructor(
    private val sync: SyncUseCase,
) {
    suspend operator fun invoke() {
        // First item in the flow (except uninitialized) is the state of last sync,
        // so it can be used to as a filter out old sync states
        sync(SyncCommands.OneTime.Events.start()).let { (startJob, syncStatusFlow) ->
            val eventSyncStateFlow = syncStatusFlow
                .map { it.eventSyncState }
            val lastSyncId = eventSyncStateFlow
                .firstOrNull { !it.isUninitialized() }
                ?.syncId
            startJob.join()
            eventSyncStateFlow
                .firstOrNull { it.syncId != lastSyncId && it.isSyncReporterCompleted() }
        }
    }
}
