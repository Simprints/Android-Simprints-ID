package com.simprints.infra.sync.config.usecase

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.sync.SyncOrchestrator
import javax.inject.Inject

internal class LogoutUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    private val authManager: AuthManager,
) {
    suspend operator fun invoke() {
        syncOrchestrator.cancelBackgroundWork()
        syncOrchestrator.deleteEventSyncInfo()
        authManager.signOut()
    }
}
