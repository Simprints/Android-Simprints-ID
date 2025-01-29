package com.simprints.fingerprint.infra.biosdk

import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse

interface BioSdkWrapper {
    // Maximum time to wait for the bio sdk to capture a fingerprint template
    val scanningTimeoutMs: Long

    // Maximum time to wait for the bio sdk to transfer the fingerprint image
    val imageTransferTimeoutMs: Long

    // Minimum Number of required good scans
    val minGoodScans: Int

    // Determines whether to suggest adding a new finger after multiple bad scans
    val addNewFingerOnBadScan: Boolean

    val matcherName: String

    val supportedTemplateFormat: String

    suspend fun initialize()

    suspend fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        isCrossFingerMatchingEnabled: Boolean,
    ): List<MatchResult>

    suspend fun acquireFingerprintTemplate(
        capturingResolution: Int?,
        timeOutMs: Int,
        qualityThreshold: Int,
        allowLowQualityExtraction: Boolean,
    ): AcquireFingerprintTemplateResponse

    suspend fun acquireFingerprintImage(): AcquireFingerprintImageResponse
}
