package com.simprints.face.infra.biosdkresolver

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.logging.Simber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResolveFaceBioSdkUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val rocV1BioSdk: RocV1BioSdk,
    private val rocV3BioSdk: RocV3BioSdk,
    private val edgeFaceBioSDK: EdgeFaceBioSdk,
    private val faceNetBioSdk: FaceNetBioSdk,
) {

    suspend operator fun invoke(): FaceBioSDK {
        val version = configRepository.getProjectConfiguration().face?.rankOne?.version
        Simber.d("FaceBioSDK version: $version")
        //   // if the version is null, return rocV1BioSdk
        //   return if (version == rocV3BioSdk.version) rocV3BioSdk else rocV1BioSdk

        // TODO For the PoC ROCv3==FaceNet, ROCv1==EdgeFace
        return if (version == rocV3BioSdk.version) faceNetBioSdk else edgeFaceBioSDK
    }
}
