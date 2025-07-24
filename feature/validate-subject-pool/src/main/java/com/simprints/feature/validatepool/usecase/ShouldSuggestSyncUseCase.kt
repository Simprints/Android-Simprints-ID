package com.simprints.feature.validatepool.usecase

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.eventsync.EventSyncManager
import javax.inject.Inject
import kotlin.time.Duration

internal class ShouldSuggestSyncUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val syncManager: EventSyncManager,
    private val configRepository: ConfigRepository,
) {
    suspend operator fun invoke(): Boolean = syncManager
        .getLastSyncTime()
        ?.let {
            val simprintsDownSyncConfig = configRepository
                .getProjectConfiguration()
                .synchronization
                .down
                .simprints
            if (simprintsDownSyncConfig == null) {
                return@let false
            }

            val thresholdMs = simprintsDownSyncConfig
                .maxAge
                .let(Duration.Companion::parse)
                .inWholeMilliseconds

            timeHelper.msBetweenNowAndTime(it) > thresholdMs
        }
        ?: true
}
