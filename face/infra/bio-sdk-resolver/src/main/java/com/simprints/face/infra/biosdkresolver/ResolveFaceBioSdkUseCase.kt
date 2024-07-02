package com.simprints.face.infra.biosdkresolver

import com.simprints.infra.config.store.ConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResolveFaceBioSdkUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val rocV1BioSdk: RocV1BioSdk,
    private val rocV3BioSdk: RocV3BioSdk
) {

    suspend operator fun invoke(): FaceBioSDK {
        val config = configRepository.getProjectConfiguration()
        // Todo we didn't yet implement the logic to select the SDK based on the configuration
        // so we are just using the v1  SDK for now
        return rocV1BioSdk

    }
}
