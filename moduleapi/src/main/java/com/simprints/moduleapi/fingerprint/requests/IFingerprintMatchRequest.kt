package com.simprints.moduleapi.fingerprint.requests

import java.io.Serializable

class IFingerprintMatchRequest: FingerprintRequest(
    val probeFingerprintSamples: List<FingerprintSample>,
    val queryForCandidates: Serializable
)

