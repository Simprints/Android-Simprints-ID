package com.simprints.fingerprint.infra.biosdk

import com.simprints.fingerprint.infra.basebiosdk.FingerprintBioSdk
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.TemplateResponse
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.toDomain
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.biosdkimpl.acquisition.template.FingerprintTemplateAcquisitionSettings
import com.simprints.fingerprint.infra.biosdkimpl.acquisition.template.FingerprintTemplateMetadata
import com.simprints.fingerprint.infra.biosdkimpl.matching.SimAfisMatcherSettings
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import javax.inject.Inject

class SimprintsBioSdkWrapper @Inject constructor(
    private val bioSdk:
        FingerprintBioSdk<Unit, Unit, Unit, FingerprintTemplateAcquisitionSettings, FingerprintTemplateMetadata, SimAfisMatcherSettings>,
) : BioSdkWrapper {
    override val scanningTimeoutMs
        get() = 3000L
    override val imageTransferTimeoutMs
        get() = 3000L

    override val minGoodScans: Int
        get() = 2
    override val addNewFingerOnBadScan: Boolean
        get() = true

    override val matcherName: String
        get() = bioSdk.matcherName
    override val supportedTemplateFormat: String
        get() = bioSdk.supportedTemplateFormat

    override suspend fun initialize() {
        bioSdk.initialize()
    }

    override suspend fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        isCrossFingerMatchingEnabled: Boolean,
    ) = bioSdk.match(probe, candidates, SimAfisMatcherSettings(isCrossFingerMatchingEnabled))

    override suspend fun acquireFingerprintTemplate(
        capturingResolution: Int?,
        timeOutMs: Int,
        qualityThreshold: Int,
        allowLowQualityExtraction: Boolean,
    ): AcquireFingerprintTemplateResponse {
        val settings = FingerprintTemplateAcquisitionSettings(
            capturingResolution?.let { Dpi(it.toShort()) },
            timeOutMs,
            qualityThreshold,
            allowLowQualityExtraction,
        )
        return bioSdk.acquireFingerprintTemplate(settings).toDomain()
    }

    override suspend fun acquireFingerprintImage() = bioSdk.acquireFingerprintImage().toDomain()
}

private fun TemplateResponse<FingerprintTemplateMetadata>.toDomain(): AcquireFingerprintTemplateResponse {
    require(templateMetadata != null) {
        "Template metadata should not be null"
    }
    return AcquireFingerprintTemplateResponse(
        template,
        templateMetadata!!.templateFormat,
        templateMetadata!!.imageQualityScore,
    )
}
