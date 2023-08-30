package com.simprints.feature.clientapi.session

import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.canSyncDataToSimprints
import javax.inject.Inject

internal class DeleteSessionEventsIfNeededUseCase @Inject constructor(
    private val configManager: ConfigManager,
    private val clientSessionManager: ClientSessionManager,
) {

    suspend operator fun invoke(sessionId: String) {
        if (!configManager.getProjectConfiguration().canSyncDataToSimprints()) {
            clientSessionManager.deleteSessionEvents(sessionId)
        }
    }
}
