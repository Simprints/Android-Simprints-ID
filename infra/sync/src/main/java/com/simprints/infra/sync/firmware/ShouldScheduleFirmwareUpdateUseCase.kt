package com.simprints.infra.sync.firmware

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.FingerprintConfiguration
import javax.inject.Inject

class ShouldScheduleFirmwareUpdateUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
) {
    suspend operator fun invoke(): Boolean = configRepository
        .getProjectConfiguration()
        .fingerprint
        ?.allowedScanners
        ?.contains(FingerprintConfiguration.VeroGeneration.VERO_2) == true
}
