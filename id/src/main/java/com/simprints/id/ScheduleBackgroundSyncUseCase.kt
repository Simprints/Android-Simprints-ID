package com.simprints.id

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import javax.inject.Inject

class ScheduleBackgroundSyncUseCase @Inject constructor(
    private val eventSyncManager: EventSyncManager,
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val configManager: ConfigManager,
    private val authManager: AuthManager,
    private val authStore: AuthStore,
) {

    operator fun invoke() {
        if (authStore.signedInProjectId.isNotEmpty()) {
            eventSyncManager.scheduleSync()
            imageUpSyncScheduler.scheduleImageUpSync()
            configManager.scheduleSyncConfiguration()
            authManager.scheduleSecurityStateCheck()
        }
    }
}