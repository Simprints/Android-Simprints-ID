package com.simprints.feature.troubleshooting.overview.usecase

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.sync.ConfigSyncCache
import javax.inject.Inject

internal class CollectConfigurationDetailsUseCase @Inject constructor(
    private val configManager: ConfigRepository,
    private val configSyncCache: ConfigSyncCache,
) {
    suspend operator fun invoke(): String {
        val config = configManager.getProjectConfiguration()
        val lastUpdate = configSyncCache.sinceLastUpdateTime()

        return """
            Configuration ID: ${config.id}
            Config updated on: ${config.updatedAt}
            Since last configuration sync: $lastUpdate
            """.trimIndent()
    }
}
