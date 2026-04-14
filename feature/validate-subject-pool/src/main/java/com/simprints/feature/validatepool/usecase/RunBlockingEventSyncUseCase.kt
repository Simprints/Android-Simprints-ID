package com.simprints.feature.validatepool.usecase

import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.extensions.await
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

internal class RunBlockingEventSyncUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
) {
    suspend operator fun invoke() {
        val lastUpSyncId = syncOrchestrator.observeUpSyncState()
            .firstOrNull { !it.isUninitialized() }
            ?.syncId
        val lastDownSyncId = syncOrchestrator.observeDownSyncState()
            .firstOrNull { !it.isUninitialized() }
            ?.syncId

        syncOrchestrator.execute(OneTime.UpSync.start()).await()
        syncOrchestrator.execute(OneTime.DownSync.start()).await()

        syncOrchestrator.observeUpSyncState()
            .firstOrNull { it.syncId != lastUpSyncId && it.hasSyncHistory() && !it.isSyncRunning() }

        syncOrchestrator.observeDownSyncState()
            .firstOrNull { it.syncId != lastDownSyncId && it.hasSyncHistory() && !it.isSyncRunning() }
    }
}
