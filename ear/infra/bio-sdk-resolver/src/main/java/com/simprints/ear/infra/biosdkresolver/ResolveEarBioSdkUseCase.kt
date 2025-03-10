package com.simprints.ear.infra.biosdkresolver

import com.simprints.infra.config.store.ConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResolveEarBioSdkUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val earBioSDK: EarSimFaceBioSdk,
) {
    suspend operator fun invoke(): EarBioSDK {
        // TODO resolve versions when necessary
        return earBioSDK
    }
}
