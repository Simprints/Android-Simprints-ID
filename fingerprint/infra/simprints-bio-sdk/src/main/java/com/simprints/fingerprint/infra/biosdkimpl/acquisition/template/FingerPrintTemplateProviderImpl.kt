package com.simprints.fingerprint.infra.biosdkimpl.acquisition.template

import com.simprints.fingerprint.infra.basebiosdk.acquization.FingerprintTemplateProvider
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintTemplateResponse
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import javax.inject.Inject

internal class FingerPrintTemplateProviderImpl @Inject constructor(
    private val fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory
) :
    FingerprintTemplateProvider<FingerprintTemplateAcquisitionSettings, FingerprintTemplateMetadata> {
    override suspend fun acquireFingerprintTemplate(settings: FingerprintTemplateAcquisitionSettings?): AcquireFingerprintTemplateResponse<FingerprintTemplateMetadata> {
        require(settings != null) { "Settings cannot be null" }
        val response = fingerprintCaptureWrapperFactory.captureWrapper.acquireFingerprintTemplate(
            settings.captureFingerprintDpi,
            settings.timeOutMs,
            settings.qualityThreshold
        )
        return AcquireFingerprintTemplateResponse(
            response.template,
            FingerprintTemplateMetadata(response.templateFormat, response.imageQualityScore)
        )
    }

}
