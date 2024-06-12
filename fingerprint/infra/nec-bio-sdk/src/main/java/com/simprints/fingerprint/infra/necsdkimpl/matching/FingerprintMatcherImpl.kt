package com.simprints.fingerprint.infra.necsdkimpl.matching

import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.basebiosdk.matching.FingerprintMatcher
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.Fingerprint
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.template.NEC_TEMPLATE_FORMAT
import javax.inject.Inject

internal class FingerprintMatcherImpl @Inject constructor(
) : FingerprintMatcher<NecMatchingSettings> {

    override val supportedTemplateFormat: String = NEC_TEMPLATE_FORMAT
    override val matcherName: String = "NEC"

    override suspend fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        settings: NecMatchingSettings?
    ): List<MatchResult> {
        // if probe template format is not supported by NEC matcher, return empty list
        if (probe.templateFormatNotSupportedByNecMatcher()) {
            return emptyList()
        }
        return            sameFingerMatching(probe, candidates)

    }

    private fun sameFingerMatching(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>
    ) = candidates.map {
        sameFingerMatching(probe, it)
    }

    /**
     * This method gets the matching score
     * the matching score is calculated by
     * - Getting the matching score for each similar finger pairs using NEC SDK
     * - The overall score is the average of the individual finger match scores
     * - We ignore probe fingers that doesn't have matching candidate fingers
     * @param probe
     * @param candidate
     * @return MatchResult
     */
    private fun sameFingerMatching(
        probe: FingerprintIdentity,
        candidate: FingerprintIdentity
    ): MatchResult {

        return MatchResult(candidate.subjectId, 0f)
    }

    private fun verify(probe: Fingerprint, candidate: Fingerprint) =0

  }

private fun FingerprintIdentity.templateFormatNotSupportedByNecMatcher(): Boolean =
    fingerprints.any { it.format != NEC_TEMPLATE_FORMAT }
