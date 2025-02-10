package com.simprints.fingerprint.infra.biosdk

import com.simprints.fingerprint.infra.basebiosdk.FingerprintBioSdk
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.TemplateResponse
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.toDomain
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.template.FingerprintTemplateAcquisitionSettings
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.template.FingerprintTemplateMetadata
import com.simprints.fingerprint.infra.necsdkimpl.matching.NecMatchingSettings
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import javax.inject.Inject

class NECBioSdkWrapper @Inject constructor(
    private val bioSdk:
        FingerprintBioSdk<Unit, Unit, Unit, FingerprintTemplateAcquisitionSettings, FingerprintTemplateMetadata, NecMatchingSettings>,
) : BioSdkWrapper {
    override val scanningTimeoutMs: Long
        get() = 8000 // 8 seconds = maximum duration for scanning + image transfer + image processing + NEC template extraction
    override val imageTransferTimeoutMs: Long
        get() = 0 // 0 seconds as the image is already captured and stored in the memory from the scanning step

    override val minGoodScans: Int
        get() = 0 // NEC SDK has a high rate of bad scans, so we don't need to enforce a minimum number of good scans

    override val addNewFingerOnBadScan: Boolean
        get() = false // NEC SDK has a high rate of bad scans, so we don't need to add a new finger on bad scan

    override val matcherName: String = bioSdk.matcherName

    override val supportedTemplateFormat: String = bioSdk.supportedTemplateFormat

    override suspend fun initialize() = bioSdk.initialize()

    override suspend fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        isCrossFingerMatchingEnabled: Boolean,
    ): List<MatchResult> = bioSdk.match(probe, candidates, NecMatchingSettings(isCrossFingerMatchingEnabled))

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

fun TemplateResponse<FingerprintTemplateMetadata>.toDomain(): AcquireFingerprintTemplateResponse {
    require(templateMetadata != null) {
        "Template metadata should not be null"
    }
    return AcquireFingerprintTemplateResponse(
        template,
        templateMetadata!!.templateFormat,
        templateMetadata!!.imageQualityScore,
    )
}
