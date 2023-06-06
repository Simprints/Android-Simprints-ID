package com.simprints.fingerprint.infra.matcher

import com.simprints.fingerprint.infra.matcher.algorithms.simafis.SimAfisMatcher
import com.simprints.fingerprint.infra.matcher.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.matcher.domain.MatchResult
import com.simprints.fingerprint.infra.matcher.domain.MatchingAlgorithm

interface FingerprintMatcher {

    /**
     * Matches a [probe] against the given flow of [candidates] using the given [matchingAlgorithm],
     * producing a flow of [MatchResult]. If the [matchingAlgorithm] supports it, designed to act
     * as a pipeline which can be fed candidates and produces match results.
     *
     * @throws IllegalArgumentException if the TemplateFormats of the supplied [probe] or
     * [candidates] is not compatible with the desired [matchingAlgorithm].
     */
    fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        matchingAlgorithm: MatchingAlgorithm,
        crossFingerComparison: Boolean
    ) : List<MatchResult>

    companion object {
        fun create(): FingerprintMatcher =
            FingerprintMatcherImpl(SimAfisMatcher())
    }
}
