package com.simprints.fingerprint.infra.basebiosdk.matching

import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.core.domain.sample.ComparisonResult

interface FingerprintMatcher<MatcherSettings> {
    /**
     * Matches a [probe] against the given flow of [candidates]
     * producing a flow of [ComparisonResult].
     *
     * @throws IllegalArgumentException if the TemplateFormats of the supplied [probe]
     */
    suspend fun match(
        probeReference: BiometricReferenceCapture,
        candidates: List<CandidateRecord>,
        settings: MatcherSettings?,
    ): List<ComparisonResult>

    val supportedTemplateFormat: String
    val matcherName: String
}
