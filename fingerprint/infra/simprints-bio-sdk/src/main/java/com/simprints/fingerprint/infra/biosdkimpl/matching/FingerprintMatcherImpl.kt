package com.simprints.fingerprint.infra.biosdkimpl.matching

import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.Identity
import com.simprints.core.domain.sample.MatchConfidence
import com.simprints.fingerprint.infra.basebiosdk.matching.FingerprintMatcher
import com.simprints.fingerprint.infra.biosdkimpl.matching.SimAfisMatcher.Companion.SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT
import javax.inject.Inject

internal class FingerprintMatcherImpl @Inject constructor(
    private val simAfisMatcher: SimAfisMatcher,
) : FingerprintMatcher<SimAfisMatcherSettings> {
    override val supportedTemplateFormat = SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT
    override val matcherName: String = "SIM_AFIS"

    override suspend fun match(
        probe: List<CaptureSample>,
        candidates: List<Identity>,
        settings: SimAfisMatcherSettings?,
    ): List<MatchConfidence> = simAfisMatcher.match(probe, candidates, settings?.crossFingerComparison ?: false)
}
