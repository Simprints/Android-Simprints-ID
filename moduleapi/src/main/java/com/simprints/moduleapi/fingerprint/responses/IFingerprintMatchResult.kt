package com.simprints.moduleapi.fingerprint.responses

interface IFingerprintMatchResult {
    val personId: String
    val confidenceScore: Float
}
