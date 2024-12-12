package com.simprints.fingerprint.infra.biosdk

import com.simprints.fingerprint.infra.biosdkimpl.SimprintsSdk
import com.simprints.fingerprint.infra.necsdkimpl.NecSdk
import com.simprints.infra.config.store.models.FingerprintConfiguration
import javax.inject.Inject

class ResolveBioSdkWrapperUseCase @Inject constructor(
    @SimprintsSdk private val simprintsWrapper: BioSdkWrapper,
    @NecSdk private val necWrapper: BioSdkWrapper,
) {
    operator fun invoke(fingerprintSdk: FingerprintConfiguration.BioSdk): BioSdkWrapper = when (fingerprintSdk) {
        FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER -> simprintsWrapper
        FingerprintConfiguration.BioSdk.NEC -> necWrapper
        else -> error("Unknown fingerprint configuration")
    }
}
