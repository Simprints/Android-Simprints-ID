package com.simprints.feature.logincheck.usecases

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.sync.ProjectConfigurationScheduler
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import javax.inject.Inject

internal class StartBackgroundSyncUseCase @Inject constructor(
    private val eventSyncManager: EventSyncManager,
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val configScheduler: ProjectConfigurationScheduler,
    private val configRepository: ConfigRepository,
    private val authManager: AuthManager,
) {

    suspend operator fun invoke() {
        eventSyncManager.scheduleSync()
        imageUpSyncScheduler.scheduleImageUpSync()
        configScheduler.scheduleSync()
        authManager.scheduleSecurityStateCheck()

        val frequency = configRepository.getProjectConfiguration().synchronization.frequency
        if (frequency == SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START) {
            eventSyncManager.sync()
        }
    }
}
