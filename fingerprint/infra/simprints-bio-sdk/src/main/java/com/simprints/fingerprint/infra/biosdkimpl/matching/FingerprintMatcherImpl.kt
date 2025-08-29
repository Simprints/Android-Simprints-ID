package com.simprints.fingerprint.infra.biosdkimpl.matching

import com.simprints.core.domain.sample.CaptureIdentity
import com.simprints.core.domain.sample.Identity
import com.simprints.fingerprint.infra.basebiosdk.matching.FingerprintMatcher
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult
import com.simprints.fingerprint.infra.biosdkimpl.matching.SimAfisMatcher.Companion.SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT
import javax.inject.Inject

internal class FingerprintMatcherImpl @Inject constructor(
    private val simAfisMatcher: SimAfisMatcher,
) : FingerprintMatcher<SimAfisMatcherSettings> {
    override val supportedTemplateFormat = SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT
    override val matcherName: String = "SIM_AFIS"

    override suspend fun match(
        probe: CaptureIdentity,
        candidates: List<Identity>,
        settings: SimAfisMatcherSettings?,
    ): List<MatchResult> = simAfisMatcher.match(probe, candidates, settings?.crossFingerComparison ?: false)
}
