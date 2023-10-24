package com.simprints.fingerprint.infra.biosdk

import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse

interface BioSdkWrapper {
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