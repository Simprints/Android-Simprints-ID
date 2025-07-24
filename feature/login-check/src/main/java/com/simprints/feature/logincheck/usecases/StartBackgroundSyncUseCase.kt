package com.simprints.feature.logincheck.usecases

import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.sync.SyncOrchestrator
import javax.inject.Inject

internal class StartBackgroundSyncUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    private val configManager: ConfigManager,
) {
    suspend operator fun invoke() {
        val frequency = configManager
            .getProjectConfiguration()
            .synchronization.down.simprints?.frequency

        syncOrchestrator.scheduleBackgroundWork(
            withDelay = frequency != Frequency.PERIODICALLY_AND_ON_SESSION_START,
        )
    }
}
