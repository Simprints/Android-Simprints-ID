package com.simprints.id.domain.matching

data class VerificationResult(val guidVerified: String, val confidence: Int, val tier: Tier)
