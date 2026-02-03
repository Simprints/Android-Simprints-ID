package com.simprints.infra.sync.config.usecase

import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.imagesUploadRequiresUnmeteredConnection
import com.simprints.infra.sync.ScheduleCommand
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.extensions.await
import javax.inject.Inject

internal class RescheduleWorkersIfConfigChangedUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
) {
    suspend operator fun invoke(
        oldConfig: ProjectConfiguration,
        newConfig: ProjectConfiguration,
    ) {
        if (shouldRescheduleImageUpload(oldConfig, newConfig)) {
            syncOrchestrator.execute(ScheduleCommand.Images.reschedule()).await()
        }
    }

    private fun shouldRescheduleImageUpload(
        oldConfig: ProjectConfiguration,
        newConfig: ProjectConfiguration,
    ) = oldConfig.imagesUploadRequiresUnmeteredConnection() != newConfig.imagesUploadRequiresUnmeteredConnection()
}
