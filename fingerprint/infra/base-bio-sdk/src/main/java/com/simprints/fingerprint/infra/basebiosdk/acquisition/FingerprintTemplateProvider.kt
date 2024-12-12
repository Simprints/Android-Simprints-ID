package com.simprints.fingerprint.infra.basebiosdk.acquisition

import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.TemplateResponse

fun interface FingerprintTemplateProvider<TemplateRequestSettings, TemplateResponseMetadata> {
    suspend fun acquireFingerprintTemplate(settings: TemplateRequestSettings?): TemplateResponse<TemplateResponseMetadata>
}
