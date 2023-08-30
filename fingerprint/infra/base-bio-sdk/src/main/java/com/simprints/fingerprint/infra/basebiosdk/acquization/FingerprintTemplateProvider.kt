package com.simprints.fingerprint.infra.basebiosdk.acquization

import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintTemplateResponse

fun interface FingerprintTemplateProvider<TemplateRequestSettings, TemplateResponseMetadata> {
    fun acquireFingerprintTemplate(settings: TemplateRequestSettings?):
        AcquireFingerprintTemplateResponse<TemplateResponseMetadata>
}
