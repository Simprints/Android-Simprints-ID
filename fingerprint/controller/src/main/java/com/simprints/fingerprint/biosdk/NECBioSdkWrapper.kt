package com.simprints.fingerprint.biosdk

import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse
import com.simprints.infra.config.domain.models.Vero2Configuration

@Suppress("unused") // This class will be used once we have the NEC SDK integrated
class NECBioSdkWrapper: BioSdkWrapper {
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
        captureFingerprintStrategy: Vero2Configuration.CaptureStrategy?,
        timeOutMs: Int,
        qualityThreshold: Int
    ): AcquireFingerprintTemplateResponse {
        TODO("Not yet implemented")
    }

    override suspend fun acquireFingerprintImage(): AcquireFingerprintImageResponse {
        TODO("Not yet implemented")
    }

}
