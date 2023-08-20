package com.simprints.fingerprint.infra.basebiosdk.acquization

import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintImageResponse

fun interface FingerprintImageProvider {
    fun acquireFingerprintImage(): AcquireFingerprintImageResponse
}
