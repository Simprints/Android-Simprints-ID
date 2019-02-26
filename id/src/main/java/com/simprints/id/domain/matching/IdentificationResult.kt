package com.simprints.id.domain.matching

data class IdentificationResult(val guidFound: String, val confidence: Int, val tier: Tier)
