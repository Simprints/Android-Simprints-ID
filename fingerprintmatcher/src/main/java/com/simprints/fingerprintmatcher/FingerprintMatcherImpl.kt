package com.simprints.fingerprintmatcher

import com.simprints.fingerprintmatcher.algorithms.simafis.SimAfisMatcher
import com.simprints.fingerprintmatcher.domain.FingerprintIdentity
import com.simprints.fingerprintmatcher.domain.MatchResult
import com.simprints.fingerprintmatcher.domain.MatchingAlgorithm

internal class FingerprintMatcherImpl(
    private val simAfisMatcher: SimAfisMatcher
) : FingerprintMatcher {

    override fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        matchingAlgorithm: MatchingAlgorithm,
        crossFingerComparison: Boolean
    ): List<MatchResult> =
        when (matchingAlgorithm) {
            MatchingAlgorithm.SIM_AFIS -> simAfisMatch(probe, candidates,crossFingerComparison)
        }

    private fun simAfisMatch(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        crossFingerComparison: Boolean
    ): List<MatchResult> {
        return simAfisMatcher.match(probe, candidates,crossFingerComparison)
    }
}
