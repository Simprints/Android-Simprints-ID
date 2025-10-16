package com.simprints.fingerprint.infra.basebiosdk.matching

import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.Identity
import com.simprints.core.domain.sample.MatchConfidence

interface FingerprintMatcher<MatcherSettings> {
    /**
     * Matches a [probe] against the given flow of [candidates]
     * producing a flow of [MatchConfidence].
     *
     * @throws IllegalArgumentException if the TemplateFormats of the supplied [probe]
     */
    suspend fun match(
        probe: List<CaptureSample>,
        candidates: List<Identity>,
        settings: MatcherSettings?,
    ): List<MatchConfidence>

    val supportedTemplateFormat: String
    val matcherName: String
}
