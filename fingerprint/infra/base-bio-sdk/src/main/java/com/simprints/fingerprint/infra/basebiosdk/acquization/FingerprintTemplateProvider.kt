package com.simprints.fingerprint.infra.basebiosdk.acquization

import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintTemplateResponse
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.CaptureFingerprintStrategy

fun interface FingerprintTemplateProvider {
    fun captureFingerprintTemplate(
        captureFingerprintStrategy: CaptureFingerprintStrategy?,
        timeOutMs: Int,
        qualityThreshold: Int
    ): AcquireFingerprintTemplateResponse
}
