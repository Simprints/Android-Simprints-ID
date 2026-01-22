package com.simprints.fingerprint.infra.biosdk

import com.simprints.fingerprint.infra.biosdkimpl.SimprintsSdk
import com.simprints.fingerprint.infra.necsdkimpl.NecSdk
import com.simprints.infra.config.store.models.ModalitySdkType
import javax.inject.Inject

class ResolveBioSdkWrapperUseCase @Inject constructor(
    @param:SimprintsSdk private val simprintsWrapper: BioSdkWrapper,
    @param:NecSdk private val necWrapper: BioSdkWrapper,
) {
    operator fun invoke(fingerprintSdk: ModalitySdkType): BioSdkWrapper = when (fingerprintSdk) {
        ModalitySdkType.SECUGEN_SIM_MATCHER -> simprintsWrapper
        ModalitySdkType.NEC -> necWrapper
        else -> error("Unknown fingerprint configuration")
    }
}
