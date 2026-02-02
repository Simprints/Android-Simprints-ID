package com.simprints.infra.sync.config.usecase

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.usecase.SyncUseCase
import javax.inject.Inject

internal class LogoutUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    private val sync: SyncUseCase,
    private val authManager: AuthManager,
) {
    suspend operator fun invoke() {
        sync(SyncCommands.ScheduleOf.Everything.stop())
        syncOrchestrator.deleteEventSyncInfo()
        authManager.signOut()
    }
}
