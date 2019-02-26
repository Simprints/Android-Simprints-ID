package com.simprints.id.domain.identification

data class VerificationResult(val guid: String, val confidence: Int, val tier: Tier)
