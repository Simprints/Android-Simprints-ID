package com.simprints.feature.validatepool.usecase

import com.simprints.infra.config.store.ConfigRepository
import javax.inject.Inject

internal class IsModuleIdNotSyncedUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
) {
    suspend operator fun invoke(moduleId: String): Boolean = configRepository
        .getDeviceConfiguration()
        .selectedModules
        .all { it.value != moduleId }
}
