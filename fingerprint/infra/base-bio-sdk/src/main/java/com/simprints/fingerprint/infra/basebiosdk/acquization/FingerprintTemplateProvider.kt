package com.simprints.fingerprint.infra.basebiosdk.acquization

import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintTemplateResponse

fun interface FingerprintTemplateProvider<RequestSettings, TemplateResponseMetadata> {
    fun acquireFingerprintTemplate(settings: RequestSettings?):
        AcquireFingerprintTemplateResponse<TemplateResponseMetadata>
}
