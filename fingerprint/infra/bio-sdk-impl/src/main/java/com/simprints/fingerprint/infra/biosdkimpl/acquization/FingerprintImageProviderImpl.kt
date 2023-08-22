package com.simprints.fingerprint.infra.biosdkimpl.acquization

import com.simprints.fingerprint.infra.basebiosdk.acquization.FingerprintImageProvider
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintImageResponse

internal class FingerprintImageProviderImpl :
    FingerprintImageProvider<Unit, Unit> {
    override fun acquireFingerprintImage(settings: Unit?): AcquireFingerprintImageResponse<Unit> {
        TODO("Not yet implemented")
    }

}
