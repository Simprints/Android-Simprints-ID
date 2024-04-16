package com.simprints.feature.dashboard.logout.usecase

import com.simprints.core.ExternalScope
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.sync.SyncOrchestrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import javax.inject.Inject

internal class LogoutUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    private val authManager: AuthManager,
    @ExternalScope private val externalScope: CoroutineScope,
) {

    // Callers to this fun should wait for the returned Deferred to complete
    // to ensure that the logout process is complete
    operator fun invoke() = externalScope.async {
        // Cancel all background sync
        syncOrchestrator.cancelBackgroundWork()
        syncOrchestrator.deleteEventSyncInfo()
        // sign out the user
        authManager.signOut()
    }

}
