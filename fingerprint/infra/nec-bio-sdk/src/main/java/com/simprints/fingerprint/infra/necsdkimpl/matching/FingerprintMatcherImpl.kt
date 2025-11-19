package com.simprints.fingerprint.infra.necsdkimpl.matching

import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.Identity
import com.simprints.core.domain.sample.MatchComparisonResult
import com.simprints.core.domain.sample.Sample
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.basebiosdk.matching.FingerprintMatcher
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
        probe: List<CaptureSample>,
        candidates: List<Identity>,
        settings: NecMatchingSettings?,
    ): List<MatchComparisonResult> {
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
        probe: List<CaptureSample>,
        candidates: List<Identity>,
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
        probe: List<CaptureSample>,
        candidate: Identity,
    ): MatchComparisonResult {
        var fingers = 0 // the number of fingers used in matching
        val total = probe.sumOf { fingerprint ->
            candidate.templateForFinger(fingerprint.identifier)?.let { candidateTemplate ->
                fingers++
                verify(fingerprint, candidateTemplate)
            } ?: 0.toDouble()
        }
        return MatchComparisonResult(candidate.subjectId, getOverallScore(total, fingers))
    }

    private fun verify(
        probe: CaptureSample,
        candidate: Sample,
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
        probe: List<CaptureSample>,
        candidates: List<Identity>,
    ) = candidates.map { crossFingerMatching(probe, it) }

    private fun crossFingerMatching(
        probe: List<CaptureSample>,
        candidate: Identity,
    ): MatchComparisonResult {
        // Number of fingers used in matching
        val fingers = probe.size
        // Sum of maximum matching score for each finger
        val total = probe.sumOf { probeTemplate ->
            candidate.samples.maxOf { candidateTemplate ->
                verify(probeTemplate, candidateTemplate)
            }
        }
        // Matching score  = total/number of fingers
        return MatchComparisonResult(candidate.subjectId, getOverallScore(total, fingers))
    }

    private fun Identity.templateForFinger(fingerId: SampleIdentifier) = samples.find { it.identifier == fingerId }

    private fun CaptureSample.toNecTemplate() = NECTemplate(template, 0) // Quality score not used

    private fun Sample.toNecTemplate() = NECTemplate(template, 0) // Quality score not used

    private fun getOverallScore(
        total: Double,
        fingers: Int,
    ) = if (fingers == 0) {
        0.toFloat()
    } else {
        (total / fingers).toFloat()
    }
}

private fun List<CaptureSample>.templateFormatNotSupportedByNecMatcher(): Boolean = any { it.format != NEC_TEMPLATE_FORMAT }
