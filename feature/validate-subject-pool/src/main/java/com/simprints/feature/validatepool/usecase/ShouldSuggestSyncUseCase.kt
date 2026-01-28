package com.simprints.feature.validatepool.usecase

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.usecase.SyncUseCase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Duration

internal class ShouldSuggestSyncUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val sync: SyncUseCase,
    private val configRepository: ConfigRepository,
) {
    suspend operator fun invoke(): Boolean = sync(SyncCommands.ObserveOnly)
        .syncStatusFlow
        .map { it.eventSyncState }
        .firstOrNull()
        ?.lastSyncTime
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
