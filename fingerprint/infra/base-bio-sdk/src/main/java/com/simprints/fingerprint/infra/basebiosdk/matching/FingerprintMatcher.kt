package com.simprints.fingerprint.infra.basebiosdk.matching

import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult

interface FingerprintMatcher<MatcherSettings> {
    /**
     * Matches a [probe] against the given flow of [candidates]
     * producing a flow of [MatchResult].
     *
     * @throws IllegalArgumentException if the TemplateFormats of the supplied [probe]
     */
    suspend fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        settings: MatcherSettings?,
    ): List<MatchResult>

    val supportedTemplateFormat: String
    val matcherName: String
}
