package com.simprints.infra.sync.firmware

import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.sync.ConfigManager
import javax.inject.Inject

class ShouldScheduleFirmwareUpdateUseCase @Inject constructor(
    private val configManager: ConfigManager,
) {
    suspend operator fun invoke(): Boolean = configManager
        .getProjectConfiguration()
        .fingerprint
        ?.allowedScanners
        ?.contains(FingerprintConfiguration.VeroGeneration.VERO_2) == true
}
