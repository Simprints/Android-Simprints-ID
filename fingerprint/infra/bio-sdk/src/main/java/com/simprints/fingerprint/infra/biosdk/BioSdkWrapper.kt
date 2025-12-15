package com.simprints.fingerprint.infra.biosdk

import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.sample.ComparisonResult
import com.simprints.core.domain.sample.Identity
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
        probeReference: BiometricReferenceCapture,
        candidates: List<Identity>,
        isCrossFingerMatchingEnabled: Boolean,
    ): List<ComparisonResult>

    suspend fun acquireFingerprintTemplate(
        capturingResolution: Int?,
        timeOutMs: Int,
        qualityThreshold: Int,
        allowLowQualityExtraction: Boolean,
    ): AcquireFingerprintTemplateResponse

    suspend fun acquireFingerprintImage(): AcquireFingerprintImageResponse
}
