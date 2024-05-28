package com.simprints.fingerprint.infra.biosdk

import com.simprints.fingerprint.infra.biosdkimpl.SimprintsSdk
import com.simprints.fingerprint.infra.necsdkimpl.NecSdk
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.sync.ConfigManager
import javax.inject.Inject

class ResolveBioSdkWrapperUseCase @Inject constructor(
    private val configManager: ConfigManager,
    @SimprintsSdk private val simprintsWrapper: BioSdkWrapper,
    @NecSdk private val necWrapper: BioSdkWrapper,
) {
    private lateinit var bioSdkWrapper: BioSdkWrapper

    suspend operator fun invoke(): BioSdkWrapper {
        if (::bioSdkWrapper.isInitialized) return bioSdkWrapper

        // Todo we didn't yet implement the logic to select the SDK based on the configuration
        // so we are just using the first allowed SDK for now
        // See tickets in SIM-81 for more details
        bioSdkWrapper =
            when (configManager.getProjectConfiguration().fingerprint?.allowedSDKs?.first()) {
                FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER -> simprintsWrapper
                FingerprintConfiguration.BioSdk.NEC -> necWrapper
                else -> error("Unknown fingerprint configuration")
            }

        return bioSdkWrapper
    }


}
