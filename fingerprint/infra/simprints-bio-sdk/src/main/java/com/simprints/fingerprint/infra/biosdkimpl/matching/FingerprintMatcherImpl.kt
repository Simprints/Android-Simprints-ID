package com.simprints.fingerprint.infra.biosdkimpl.matching

import com.simprints.fingerprint.infra.basebiosdk.matching.FingerprintMatcher
import com.simprints.fingerprint.infra.basebiosdk.matching.SimAfisMatcher
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult
import javax.inject.Inject

class FingerprintMatcherImpl @Inject constructor(
    private val simAfisMatcher: SimAfisMatcher
) : FingerprintMatcher<SimAfisMatcherSettings> {

    override suspend fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        settings: SimAfisMatcherSettings?
    ): List<MatchResult> = simAfisMatcher.match(probe, candidates, settings?.crossFingerComparison?:false)
}

data class SimAfisMatcherSettings(var crossFingerComparison: Boolean = false)
