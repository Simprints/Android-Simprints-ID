package com.simprints.feature.validatepool.usecase

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.ConfigRepository
import javax.inject.Inject

internal class IsModuleIdNotSyncedUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
) {
    suspend operator fun invoke(moduleId: TokenizableString): Boolean = configRepository
        .getDeviceConfiguration()
        .selectedModules
        .all { it != moduleId }
}
