package com.simprints.fingerprintmatcher

import com.simprints.fingerprintmatcher.algorithms.simafis.SimAfisMatcher
import com.simprints.fingerprintmatcher.domain.FingerprintIdentity
import com.simprints.fingerprintmatcher.domain.MatchResult
import com.simprints.fingerprintmatcher.domain.MatchingAlgorithm
import kotlinx.coroutines.flow.Flow

interface FingerprintMatcher {

    /**
     * Matches a [probe] against the given flow of [candidates] using the given [matchingAlgorithm],
     * producing a flow of [MatchResult]. If the [matchingAlgorithm] supports it, designed to act
     * as a pipeline which can be fed candidates and produces match results.
     *
     * @throws IllegalArgumentException if the TemplateFormats of the supplied [probe] or
     * [candidates] is not compatible with the desired [matchingAlgorithm].
     */
    suspend fun match(
        probe: FingerprintIdentity,
        candidates: Flow<FingerprintIdentity>,
        matchingAlgorithm: MatchingAlgorithm
    ) : Flow<MatchResult>

    companion object {
        fun create(): FingerprintMatcher =
            FingerprintMatcherImpl(SimAfisMatcher())
    }
}
