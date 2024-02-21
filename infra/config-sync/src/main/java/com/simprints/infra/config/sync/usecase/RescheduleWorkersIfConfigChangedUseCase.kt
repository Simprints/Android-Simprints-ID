package com.simprints.infra.config.sync.usecase

import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.imagesUploadRequiresUnmeteredConnection
import com.simprints.infra.images.ImageUpSyncScheduler
import javax.inject.Inject

internal class RescheduleWorkersIfConfigChangedUseCase @Inject constructor(
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
) {

    suspend operator fun invoke(
        oldConfig: ProjectConfiguration,
        newConfig: ProjectConfiguration,
    ) {
        if (shouldRescheduleImageUpload(oldConfig, newConfig)) {
            imageUpSyncScheduler.rescheduleImageUpSync()
        }
    }

    private fun shouldRescheduleImageUpload(
        oldConfig: ProjectConfiguration,
        newConfig: ProjectConfiguration,
    ) =
        oldConfig.imagesUploadRequiresUnmeteredConnection() != newConfig.imagesUploadRequiresUnmeteredConnection()
}
