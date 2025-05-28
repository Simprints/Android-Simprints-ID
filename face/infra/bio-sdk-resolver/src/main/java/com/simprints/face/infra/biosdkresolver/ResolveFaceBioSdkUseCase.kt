package com.simprints.face.infra.biosdkresolver

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.FaceConfiguration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResolveFaceBioSdkUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val rocV1BioSdk: RocV1BioSdk,
    private val rocV3BioSdk: RocV3BioSdk,
    private val simFaceBioSdk: SimFaceBioSdk,
) {
    suspend operator fun invoke(bioSdk: FaceConfiguration.BioSdk): FaceBioSDK = when (bioSdk) {
        FaceConfiguration.BioSdk.SIM_FACE -> simFaceBioSdk
        FaceConfiguration.BioSdk.RANK_ONE -> {
            val version = configRepository
                .getProjectConfiguration()
                .face
                ?.rankOne
                ?.version
                ?.takeIf { it.isNotBlank() } // Ensures version is not null or empty
            requireNotNull(version) { "FaceBioSDK version is null or empty" }
            if (version == rocV3BioSdk.version) rocV3BioSdk else rocV1BioSdk
        }
    }
}
