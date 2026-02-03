package com.simprints.feature.logincheck.usecases

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.sync.ScheduleCommand
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.extensions.await
import javax.inject.Inject

internal class StartBackgroundSyncUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val syncOrchestrator: SyncOrchestrator,
) {
    suspend operator fun invoke() {
        val frequency = configRepository
            .getProjectConfiguration()
            .synchronization.down.simprints
            ?.frequency

        val withDelay = frequency != Frequency.PERIODICALLY_AND_ON_SESSION_START
        syncOrchestrator.executeSchedulingCommand(ScheduleCommand.Everything.reschedule(withDelay)).await()
    }
}
