package com.simprints.fingerprint.infra.necsdkimpl.matching

import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.capture.BiometricTemplateCapture
import com.simprints.core.domain.comparison.ComparisonResult
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.reference.CandidateRecord
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
        probeReference: BiometricReferenceCapture,
        candidates: List<CandidateRecord>,
        settings: NecMatchingSettings?,
    ): List<ComparisonResult> {
        // if probe template format is not supported by NEC matcher, return empty list
        if (probeReference.templateFormatNotSupportedByNecMatcher()) {
            return emptyList()
        }
        val probeTemplates = probeReference.templates

        return if (settings?.crossFingerComparison == true) {
            crossFingerMatching(probeTemplates, candidates)
        } else {
            sameFingerMatching(probeTemplates, candidates)
        }
    }

    private fun sameFingerMatching(
        probe: List<BiometricTemplateCapture>,
        candidates: List<CandidateRecord>,
    ) = candidates.map {
        sameFingerMatching(probe, it)
    }

    /**
     * This method gets the matching score
     * the matching score is calculated by
     * - Getting the matching score for each similar finger pairs using NEC SDK
     * - The overall score is the average of the individual finger match scores
     * - We ignore probe fingers that doesn't have matching candidate fingers
     * @param probeTemplates
     * @param candidate
     * @return MatchResult
     */
    private fun sameFingerMatching(
        probeTemplates: List<BiometricTemplateCapture>,
        candidate: CandidateRecord,
    ): ComparisonResult {
        var fingers = 0 // the number of fingers used in matching
        val candidateTemplates = candidate.references.flatMap { it.templates }

        val total = probeTemplates.sumOf { probeTemplate ->
            candidateTemplates.find { it.identifier == probeTemplate.identifier }?.let { candidateTemplate ->
                fingers++
                verify(probeTemplate, candidateTemplate)
            } ?: 0.toDouble()
        }
        return ComparisonResult(candidate.subjectId, getOverallScore(total, fingers))
    }

    private fun verify(
        probe: BiometricTemplateCapture,
        candidate: BiometricTemplate,
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
        probe: List<BiometricTemplateCapture>,
        candidates: List<CandidateRecord>,
    ) = candidates.map { crossFingerMatching(probe, it) }

    private fun crossFingerMatching(
        probe: List<BiometricTemplateCapture>,
        candidate: CandidateRecord,
    ): ComparisonResult {
        // Number of fingers used in matching
        val fingers = probe.size
        val candidateTemplates = candidate.references.flatMap { it.templates }
        // Sum of maximum matching score for each finger
        val total = probe.sumOf { probeTemplate ->
            candidateTemplates.maxOf { candidateTemplate -> verify(probeTemplate, candidateTemplate) }
        }
        // Matching score  = total/number of fingers
        return ComparisonResult(candidate.subjectId, getOverallScore(total, fingers))
    }

    private fun BiometricTemplate.toNecTemplate() = NECTemplate(template, 0) // Quality score not used

    private fun BiometricTemplateCapture.toNecTemplate() = NECTemplate(template, 0) // Quality score not used

    private fun getOverallScore(
        total: Double,
        fingers: Int,
    ) = if (fingers == 0) {
        0.toFloat()
    } else {
        (total / fingers).toFloat()
    }
}

private fun BiometricReferenceCapture.templateFormatNotSupportedByNecMatcher(): Boolean = format != NEC_TEMPLATE_FORMAT
