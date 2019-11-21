package com.simprints.moduleapi.fingerprint.requests

import com.simprints.moduleapi.fingerprint.IFingerprintSample
import java.io.Serializable

interface IFingerprintMatchRequest : IFingerprintRequest {
    val probeFingerprintSamples: List<IFingerprintSample>
    val queryForCandidates: Serializable
}
