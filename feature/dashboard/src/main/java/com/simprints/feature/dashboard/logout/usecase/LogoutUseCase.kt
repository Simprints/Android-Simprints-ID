package com.simprints.feature.dashboard.logout.usecase

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.sync.SyncOrchestrator
import javax.inject.Inject

internal class LogoutUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    private val eventSyncManager: EventSyncManager,
    private val authManager: AuthManager,
) {

    suspend operator fun invoke() {
        // Cancel all background sync
        eventSyncManager.cancelScheduledSync()
        syncOrchestrator.cancelBackgroundWork()
        eventSyncManager.deleteSyncInfo()
        authManager.signOut()
    }
}
