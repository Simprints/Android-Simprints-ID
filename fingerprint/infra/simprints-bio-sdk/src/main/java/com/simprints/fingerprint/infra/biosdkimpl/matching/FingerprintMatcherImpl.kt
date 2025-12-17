package com.simprints.fingerprint.infra.biosdkimpl.matching

import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.comparison.ComparisonResult
import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.fingerprint.infra.basebiosdk.matching.FingerprintMatcher
import com.simprints.fingerprint.infra.biosdkimpl.matching.SimAfisMatcher.Companion.SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT
import javax.inject.Inject

internal class FingerprintMatcherImpl @Inject constructor(
    private val simAfisMatcher: SimAfisMatcher,
) : FingerprintMatcher<SimAfisMatcherSettings> {
    override val supportedTemplateFormat = SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT
    override val matcherName: String = "SIM_AFIS"

    override suspend fun match(
        probeReference: BiometricReferenceCapture,
        candidates: List<CandidateRecord>,
        settings: SimAfisMatcherSettings?,
    ): List<ComparisonResult> = simAfisMatcher.match(
        probeReference = probeReference,
        candidates = candidates,
        crossFingerComparison = settings?.crossFingerComparison ?: false,
    )
}
