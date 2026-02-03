package com.simprints.infra.sync.config.usecase

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.ScheduleCommand
import javax.inject.Inject

internal class LogoutUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    private val authManager: AuthManager,
) {
    suspend operator fun invoke() {
        syncOrchestrator.executeSchedulingCommand(ScheduleCommand.Everything.unschedule())
        syncOrchestrator.deleteEventSyncInfo()
        authManager.signOut()
    }
}
