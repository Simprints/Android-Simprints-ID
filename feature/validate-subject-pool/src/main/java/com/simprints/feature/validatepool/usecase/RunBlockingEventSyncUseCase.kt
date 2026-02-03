package com.simprints.feature.validatepool.usecase

import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.extensions.await
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class RunBlockingEventSyncUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
) {
    suspend operator fun invoke() {
        val syncState = syncOrchestrator.observeSyncState()
        // First item in the flow (except uninitialized) is the state of last sync,
        // so it can be used to as a filter out old sync states.
        // To guarantee it's not associated with the newly run sync,
        // the value needs to be taken before it starts.
        val lastSyncId = syncState
            .map { it.eventSyncState }
            .firstOrNull { !it.isUninitialized() }
            ?.syncId
        syncOrchestrator
            .executeOneTime(OneTime.Events.start())
            .await()
        syncState
            .map { it.eventSyncState }
            .firstOrNull { it.syncId != lastSyncId && it.isSyncReporterCompleted() }
    }
}
