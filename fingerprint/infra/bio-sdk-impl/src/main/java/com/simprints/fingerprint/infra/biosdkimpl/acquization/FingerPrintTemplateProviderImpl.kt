package com.simprints.fingerprint.infra.biosdkimpl.acquization

import com.simprints.fingerprint.infra.basebiosdk.acquization.FingerprintTemplateProvider
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintTemplateResponse

internal class FingerPrintTemplateProviderImpl: FingerprintTemplateProvider {
    override fun acquireFingerprintTemplate(): AcquireFingerprintTemplateResponse {
        //Todo Will add proper implementation later
        return AcquireFingerprintTemplateResponse(
            template = byteArrayOf(),
            templateMetadata = mapOf()
        )
    }
}
