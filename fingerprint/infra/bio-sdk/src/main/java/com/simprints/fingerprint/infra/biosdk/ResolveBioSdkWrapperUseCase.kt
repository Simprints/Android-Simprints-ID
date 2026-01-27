package com.simprints.fingerprint.infra.biosdk

import com.simprints.fingerprint.infra.biosdkimpl.SimprintsSdk
import com.simprints.infra.config.store.models.ModalitySdkType
import javax.inject.Inject

class ResolveBioSdkWrapperUseCase @Inject constructor(
    @param:SimprintsSdk private val simprintsWrapper: BioSdkWrapper,
) {
    operator fun invoke(fingerprintSdk: ModalitySdkType): BioSdkWrapper = when (fingerprintSdk) {
        ModalitySdkType.SECUGEN_SIM_MATCHER -> simprintsWrapper
        ModalitySdkType.NEC -> error("NEC not supported since v2026.1.0")
        else -> error("Unknown fingerprint configuration")
    }
}
