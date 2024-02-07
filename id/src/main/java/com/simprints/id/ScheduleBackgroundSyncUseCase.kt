package com.simprints.id

import com.simprints.fingerprint.infra.scanner.data.worker.FirmwareFileUpdateScheduler
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ProjectConfigurationScheduler
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import javax.inject.Inject

class ScheduleBackgroundSyncUseCase @Inject constructor(
    private val eventSyncManager: EventSyncManager,
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val configScheduler: ProjectConfigurationScheduler,
    private val authStore: AuthStore,
    private val firmwareFileUpdateScheduler: FirmwareFileUpdateScheduler,
) {

    operator fun invoke() {
        if (authStore.signedInProjectId.isNotEmpty()) {
            eventSyncManager.scheduleSync()
            imageUpSyncScheduler.scheduleImageUpSync()
            configScheduler.scheduleProjectSync()
            configScheduler.scheduleDeviceSync()
            firmwareFileUpdateScheduler.scheduleOrCancelWorkIfNecessary()
        }
    }
}
