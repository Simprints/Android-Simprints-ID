package com.simprints.fingerprint.infra.biosdkimpl.acquization

import com.simprints.fingerprint.infra.basebiosdk.acquization.FingerprintTemplateProvider
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintTemplateResponse

internal class FingerPrintTemplateProviderImpl :
    FingerprintTemplateProvider<Nothing,Nothing> {
    override fun acquireFingerprintTemplate(settings: Nothing?): AcquireFingerprintTemplateResponse<Nothing> {
        TODO("Not yet implemented")
    }

}
