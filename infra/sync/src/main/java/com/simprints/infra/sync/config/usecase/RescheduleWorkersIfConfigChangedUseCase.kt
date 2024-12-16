package com.simprints.infra.sync.config.usecase

import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.imagesUploadRequiresUnmeteredConnection
import com.simprints.infra.sync.SyncOrchestrator
import javax.inject.Inject

internal class RescheduleWorkersIfConfigChangedUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
) {
    suspend operator fun invoke(
        oldConfig: ProjectConfiguration,
        newConfig: ProjectConfiguration,
    ) {
        if (shouldRescheduleImageUpload(oldConfig, newConfig)) {
            syncOrchestrator.rescheduleImageUpSync()
        }
    }

    private fun shouldRescheduleImageUpload(
        oldConfig: ProjectConfiguration,
        newConfig: ProjectConfiguration,
    ) = oldConfig.imagesUploadRequiresUnmeteredConnection() != newConfig.imagesUploadRequiresUnmeteredConnection()
}
