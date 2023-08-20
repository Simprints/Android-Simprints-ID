package com.simprints.fingerprint.infra.biosdkimpl.matching

import com.simprints.fingerprint.infra.basebiosdk.matching.FingerprintMatcher
import com.simprints.fingerprint.infra.basebiosdk.matching.SimAfisMatcher
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult
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
