package com.simprints.fingerprint.infra.biosdkimpl.matching

import com.simprints.fingerprint.infra.basebiosdk.matching.FingerprintMatcher
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult
import javax.inject.Inject

internal class FingerprintMatcherImpl @Inject constructor(
) : FingerprintMatcher<SimAfisMatcherSettings> {

    override val supportedTemplateFormat = "Not used"
    override val matcherName: String = "SIM_AFIS"

    override suspend fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        settings: SimAfisMatcherSettings?
    ): List<MatchResult> = emptyList()
}


