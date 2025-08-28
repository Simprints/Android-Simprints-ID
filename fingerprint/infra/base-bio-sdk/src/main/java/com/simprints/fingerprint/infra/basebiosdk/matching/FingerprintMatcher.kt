package com.simprints.fingerprint.infra.basebiosdk.matching

import com.simprints.core.domain.sample.CaptureIdentity
import com.simprints.core.domain.sample.Identity
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult

interface FingerprintMatcher<MatcherSettings> {
    /**
     * Matches a [probe] against the given flow of [candidates]
     * producing a flow of [MatchResult].
     *
     * @throws IllegalArgumentException if the TemplateFormats of the supplied [probe]
     */
    suspend fun match(
        probe: CaptureIdentity,
        candidates: List<Identity>,
        settings: MatcherSettings?,
    ): List<MatchResult>

    val supportedTemplateFormat: String
    val matcherName: String
}
