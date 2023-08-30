package com.simprints.fingerprint.biosdk

import com.simprints.fingerprint.data.domain.fingerprint.toDomain
import com.simprints.fingerprint.infra.basebiosdk.FingerprintBioSdk
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintTemplateResponse
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.biosdkimpl.acquisition.template.FingerprintTemplateAcquisitionSettings
import com.simprints.fingerprint.infra.biosdkimpl.acquisition.template.FingerprintTemplateMetadata
import com.simprints.fingerprint.infra.biosdkimpl.matching.SimAfisMatcherSettings
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.infra.config.domain.models.Vero2Configuration
import javax.inject.Inject
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintImageResponse as DomainFingerprintImageResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse as DomainFingerprintTemplateResponse


class SimprintsBioSdkWrapper @Inject constructor(
    private val scannerManager: ScannerManager,
    private val bioSdk: FingerprintBioSdk<Unit, Unit, Unit, FingerprintTemplateAcquisitionSettings, FingerprintTemplateMetadata, SimAfisMatcherSettings>
) : BioSdkWrapper {


    override suspend fun initialize() {
        bioSdk.initialize()
    }

    override suspend fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>, isCrossFingerMatchingEnabled: Boolean
    ) = bioSdk.match(probe, candidates, SimAfisMatcherSettings(isCrossFingerMatchingEnabled))

    override suspend fun acquireFingerprintTemplate(
        captureFingerprintStrategy: Vero2Configuration.CaptureStrategy?,
        timeOutMs: Int,
        qualityThreshold: Int
    ): DomainFingerprintTemplateResponse {
        val settings = FingerprintTemplateAcquisitionSettings(
            captureFingerprintStrategy?.toDomain(),
            timeOutMs,
            qualityThreshold
        )
        return bioSdk.acquireFingerprintTemplate(settings).toDomain()
    }

    override suspend fun acquireFingerprintImage(): DomainFingerprintImageResponse {
        return bioSdk.acquireFingerprintImage().toDomain()
    }
}

private fun AcquireFingerprintImageResponse<Unit>.toDomain()=
    DomainFingerprintImageResponse(this.imageBytes)

private fun AcquireFingerprintTemplateResponse<FingerprintTemplateMetadata>.toDomain(): DomainFingerprintTemplateResponse {
    require(templateMetadata != null) {
        "Template metadata should not be null"
    }
    return DomainFingerprintTemplateResponse(
        template,
        templateMetadata!!.templateFormat,
        templateMetadata!!.imageQualityScore
    )
}
