package com.simprints.moduleapi.fingerprint.requests

import java.io.Serializable

interface IFingerprintMatchRequest {
    val probeFingerprintSamples: List<IFingerprintSample>
    val queryForCandidates: Serializable
}
