package com.simprints.fingerprint.infra.biosdkimpl.acquization

import com.simprints.fingerprint.infra.basebiosdk.acquization.FingerprintTemplateProvider
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintTemplateResponse

internal class FingerPrintTemplateProviderImpl :
    FingerprintTemplateProvider<Unit,Unit> {
    override fun acquireFingerprintTemplate(settings: Unit?): AcquireFingerprintTemplateResponse<Unit> {
        TODO("Not yet implemented")
    }

}
