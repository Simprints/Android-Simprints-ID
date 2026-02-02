package com.simprints.feature.logincheck.usecases

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.await
import com.simprints.infra.sync.usecase.SyncUseCase
import javax.inject.Inject

internal class StartBackgroundSyncUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val sync: SyncUseCase,
) {
    suspend operator fun invoke() {
        val frequency = configRepository
            .getProjectConfiguration()
            .synchronization.down.simprints
            ?.frequency

        val withDelay = frequency != Frequency.PERIODICALLY_AND_ON_SESSION_START
        sync(SyncCommands.ScheduleOf.Everything.start(withDelay)).await()
    }
}
