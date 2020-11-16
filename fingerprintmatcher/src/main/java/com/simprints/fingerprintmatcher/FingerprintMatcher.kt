package com.simprints.fingerprintmatcher

import com.simprints.fingerprintmatcher.algorithms.simafis.SimAfisMatcher
import kotlinx.coroutines.flow.Flow

interface FingerprintMatcher {

    suspend fun match(
        probe: FingerprintRecord,
        candidates: Flow<FingerprintRecord>,
        matchingAlgorithm: MatchingAlgorithm
    ) : Flow<MatchResult>

    companion object {
        fun create(): FingerprintMatcher =
            FingerprintMatcherImpl(SimAfisMatcher())
    }
}
