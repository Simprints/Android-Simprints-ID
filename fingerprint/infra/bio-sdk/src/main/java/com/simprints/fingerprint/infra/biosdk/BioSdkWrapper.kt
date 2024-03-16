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

    val matcherName: String

    val supportedTemplateFormat: String

    suspend fun initialize()

    suspend fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>, isCrossFingerMatchingEnabled: Boolean
    ): List<MatchResult>

    suspend fun acquireFingerprintTemplate(
        capturingResolution: Int?,
        timeOutMs: Int,
        qualityThreshold: Int
    ): AcquireFingerprintTemplateResponse

    suspend fun acquireFingerprintImage(): AcquireFingerprintImageResponse

}
