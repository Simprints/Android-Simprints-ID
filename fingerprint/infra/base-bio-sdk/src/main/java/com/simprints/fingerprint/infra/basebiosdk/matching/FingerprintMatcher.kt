package com.simprints.fingerprint.infra.basebiosdk.matching


import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult

fun interface FingerprintMatcher<MatcherSettings> {

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
        settings: MatcherSettings?
    ) : List<MatchResult>

}
