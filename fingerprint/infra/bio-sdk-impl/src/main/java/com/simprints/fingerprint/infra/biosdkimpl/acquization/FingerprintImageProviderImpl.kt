package com.simprints.fingerprint.infra.biosdkimpl.acquization

import com.simprints.fingerprint.infra.basebiosdk.acquization.FingerprintImageProvider
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintImageResponse

internal class FingerprintImageProviderImpl : FingerprintImageProvider {
    override fun acquireFingerprintImage(): AcquireFingerprintImageResponse {
        //Todo Will add proper implementation later
        return AcquireFingerprintImageResponse(byteArrayOf(), mapOf())
    }
}
