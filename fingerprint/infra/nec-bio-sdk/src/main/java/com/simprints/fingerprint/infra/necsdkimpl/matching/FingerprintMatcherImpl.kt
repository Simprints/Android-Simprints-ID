package com.simprints.fingerprint.infra.necsdkimpl.matching

import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.basebiosdk.matching.FingerprintMatcher
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.Fingerprint
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.template.NEC_TEMPLATE_FORMAT
import com.simprints.necwrapper.nec.NEC
import com.simprints.necwrapper.nec.models.NECTemplate
import javax.inject.Inject

internal class FingerprintMatcherImpl @Inject constructor(
    private val nec: NEC,
) : FingerprintMatcher<NecMatchingSettings> {
    override val supportedTemplateFormat: String = NEC_TEMPLATE_FORMAT
    override val matcherName: String = "NEC"

    override suspend fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        settings: NecMatchingSettings?,
    ): List<MatchResult> {
        // if probe template format is not supported by NEC matcher, return empty list
        if (probe.templateFormatNotSupportedByNecMatcher()) {
            return emptyList()
        }
        return if (settings?.crossFingerComparison == true) {
            crossFingerMatching(probe, candidates)
        } else {
            sameFingerMatching(probe, candidates)
        }
    }

    private fun sameFingerMatching(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
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
        candidate: FingerprintIdentity,
    ): MatchResult {
        var fingers = 0 // the number of fingers used in matching
        val total = probe.fingerprints.sumOf { fingerprint ->
            candidate.templateForFinger(fingerprint.fingerId)?.let { candidateTemplate ->
                fingers++
                verify(fingerprint, candidateTemplate)
            } ?: 0.toDouble()
        }
        return MatchResult(candidate.subjectId, getOverallScore(total, fingers))
    }

    private fun verify(
        probe: Fingerprint,
        candidate: Fingerprint,
    ) = try {
        nec
            .match(
                probe.toNecTemplate(),
                candidate.toNecTemplate(),
            ).toDouble()
    } catch (e: Exception) {
        throw BioSdkException.TemplateMatchingException(e)
    }

    private fun crossFingerMatching(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
    ) = candidates.map { crossFingerMatching(probe, it) }

    private fun crossFingerMatching(
        probe: FingerprintIdentity,
        candidate: FingerprintIdentity,
    ): MatchResult {
        // Number of fingers used in matching
        val fingers = probe.fingerprints.size
        // Sum of maximum matching score for each finger
        val total = probe.fingerprints.sumOf { probeTemplate ->
            candidate.fingerprints.maxOf { candidateTemplate ->
                verify(probeTemplate, candidateTemplate)
            }
        }
        // Matching score  = total/number of fingers
        return MatchResult(candidate.subjectId, getOverallScore(total, fingers))
    }

    private fun FingerprintIdentity.templateForFinger(fingerId: FingerIdentifier) = fingerprints.find { it.fingerId == fingerId }

    private fun Fingerprint.toNecTemplate() = NECTemplate(byteArrayOf(), 0) // Quality score not used

    private fun getOverallScore(
        total: Double,
        fingers: Int,
    ) = if (fingers == 0) {
        0.toFloat()
    } else {
        (total / fingers).toFloat()
    }
}

private fun FingerprintIdentity.templateFormatNotSupportedByNecMatcher(): Boolean = fingerprints.any { it.format != NEC_TEMPLATE_FORMAT }
