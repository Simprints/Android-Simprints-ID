package com.simprints.fingerprint.infra.biosdk

import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse

@Suppress("unused") // This class will be used once we have the NEC SDK integrated
internal class NECBioSdkWrapper: BioSdkWrapper {
    override suspend fun initialize() {
        TODO("Not yet implemented")
    }

    override suspend fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        isCrossFingerMatchingEnabled: Boolean
    ): List<MatchResult> {
        TODO("Not yet implemented")
    }

    override suspend fun acquireFingerprintTemplate(
        capturingResolution: Int?,
        timeOutMs: Int,
        qualityThreshold: Int
    ): AcquireFingerprintTemplateResponse {
        TODO("Not yet implemented")
    }

    override suspend fun acquireFingerprintImage(): AcquireFingerprintImageResponse {
        TODO("Not yet implemented")
    }

}
