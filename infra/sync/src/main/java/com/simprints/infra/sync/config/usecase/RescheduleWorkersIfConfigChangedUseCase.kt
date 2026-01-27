package com.simprints.infra.sync.config.usecase

import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.imagesUploadRequiresUnmeteredConnection
import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.await
import com.simprints.infra.sync.usecase.SyncUseCase
import javax.inject.Inject

internal class RescheduleWorkersIfConfigChangedUseCase @Inject constructor(
    private val sync: SyncUseCase,
) {
    suspend operator fun invoke(
        oldConfig: ProjectConfiguration,
        newConfig: ProjectConfiguration,
    ) {
        if (shouldRescheduleImageUpload(oldConfig, newConfig)) {
            sync(SyncCommands.Schedule.Images.start()).await()
        }
    }

    private fun shouldRescheduleImageUpload(
        oldConfig: ProjectConfiguration,
        newConfig: ProjectConfiguration,
    ) = oldConfig.imagesUploadRequiresUnmeteredConnection() != newConfig.imagesUploadRequiresUnmeteredConnection()
}
