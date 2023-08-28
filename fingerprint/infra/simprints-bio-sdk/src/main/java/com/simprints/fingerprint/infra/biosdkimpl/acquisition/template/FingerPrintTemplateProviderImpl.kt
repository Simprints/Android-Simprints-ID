package com.simprints.fingerprint.infra.biosdkimpl.acquisition.template

import com.simprints.fingerprint.infra.basebiosdk.acquization.FingerprintTemplateProvider
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintTemplateResponse

internal class FingerPrintTemplateProviderImpl:
    FingerprintTemplateProvider<FingerprintTemplateAcquisitionSettings, FingerprintTemplateMetadata> {
    override fun acquireFingerprintTemplate(settings: FingerprintTemplateAcquisitionSettings?): AcquireFingerprintTemplateResponse<FingerprintTemplateMetadata> {
        TODO()
    }

}
