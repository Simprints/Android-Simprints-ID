package com.simprints.feature.dashboard.logout.usecase

import com.simprints.core.ExternalScope
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.sync.SyncOrchestrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class LogoutUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    private val authManager: AuthManager,
    @ExternalScope private val externalScope: CoroutineScope,
) {

    suspend operator fun invoke() {
        authManager.signOut()

        // Cancel all background sync
        externalScope.launch {
            syncOrchestrator.cancelBackgroundWork()
            syncOrchestrator.deleteEventSyncInfo()
        }
    }
}
