package com.simprints.fingerprint.infra.matcher

import com.simprints.fingerprint.infra.matcher.algorithms.simafis.SimAfisMatcher
import com.simprints.fingerprint.infra.matcher.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.matcher.domain.MatchResult
import javax.inject.Inject

internal class FingerprintMatcherImpl @Inject constructor(
    private val simAfisMatcher: SimAfisMatcher
) : FingerprintMatcher {

    override fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        crossFingerComparison: Boolean
    ): List<MatchResult> = simAfisMatch(probe, candidates, crossFingerComparison)


    private fun simAfisMatch(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        crossFingerComparison: Boolean
    ): List<MatchResult> {
        return simAfisMatcher.match(probe, candidates, crossFingerComparison)
    }
}
