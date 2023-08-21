package com.simprints.fingerprint.infra.biosdkimpl.acquization

import com.simprints.fingerprint.infra.basebiosdk.acquization.FingerprintImageProvider
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintImageResponse

internal class FingerprintImageProviderImpl :
    FingerprintImageProvider<Nothing, Nothing> {
    override fun acquireFingerprintImage(settings: Nothing?): AcquireFingerprintImageResponse<Nothing> {
        TODO("Not yet implemented")
    }

}
