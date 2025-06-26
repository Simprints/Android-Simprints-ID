package com.simprints.face.infra.biosdkresolver

import com.simprints.infra.config.store.ConfigRepository
import javax.inject.Inject

internal class ResolveRankOneVersionUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val rocV1BioSdk: RocV1BioSdk,
    private val rocV3BioSdk: RocV3BioSdk,
) {
    suspend operator fun invoke(): FaceBioSDK {
        val version = configRepository
            .getProjectConfiguration()
            .face
            ?.rankOne
            ?.version
            ?.takeIf { it.isNotBlank() } // Ensures version is not null or empty
        requireNotNull(version) { "FaceBioSDK version is null or empty" }
        return if (version == rocV3BioSdk.version()) rocV3BioSdk else rocV1BioSdk
    }
}
