package com.simprints.infra.config.sync.usecase

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.config.sync.ProjectConfigurationScheduler
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import javax.inject.Inject

internal class LogoutUseCase @Inject constructor(
    private val configScheduler: ProjectConfigurationScheduler,
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val eventSyncManager: EventSyncManager,
    private val authManager: AuthManager,
) {

    suspend operator fun invoke() {
        imageUpSyncScheduler.cancelImageUpSync()
        configScheduler.cancelProjectSync()
        configScheduler.cancelDeviceSync()
        eventSyncManager.cancelScheduledSync()
        eventSyncManager.deleteSyncInfo()
        authManager.signOut()
    }
}
