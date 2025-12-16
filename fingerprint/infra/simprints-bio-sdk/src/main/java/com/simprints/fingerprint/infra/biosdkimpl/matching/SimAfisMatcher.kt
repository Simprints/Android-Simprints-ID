package com.simprints.fingerprint.infra.biosdkimpl.matching

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.capture.BiometricTemplateCapture
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.core.domain.comparison.ComparisonResult
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.fingerprint.infra.simafiswrapper.JNILibAfisInterface
import com.simprints.fingerprint.infra.simafiswrapper.models.SimAfisFingerIdentifier
import com.simprints.fingerprint.infra.simafiswrapper.models.SimAfisFingerprint
import com.simprints.fingerprint.infra.simafiswrapper.models.SimAfisPerson
import java.nio.ByteBuffer
import javax.inject.Inject

/**
 * SimAFIS is Simprints' improvement over SourceAFIS, an open source fingerprint matching algorithm
 * originally written in Java. It has been ported to C and optimized for use on Android.
 *
 * It is multithreaded by default. It currently does not support receiving a pipeline of candidates
 * as they are loaded from the DB and instead requires all candidates to be presented up front as a
 * list. It does not currently support progress indication and matching results are only available
 * when all matching is completed.
 */
internal class SimAfisMatcher @Inject constructor(
    private val jniLibAfis: JNILibAfisInterface,
) {
    fun match(
        probeReference: BiometricReferenceCapture,
        candidates: List<CandidateRecord>,
        crossFingerComparison: Boolean,
    ): List<ComparisonResult> {
        // if probe template format is not supported by SimAfisMatcher, return empty list
        if (probeReference.templateFormatNotSupportedBySimAfisMatcher()) {
            return emptyList()
        }
        val probeTemplates = probeReference.templates

        return if (crossFingerComparison) {
            crossFingerMatch(probeTemplates, candidates)
        } else {
            match(probeTemplates, candidates)
        }
    }

    private fun match(
        probe: List<BiometricTemplateCapture>,
        candidates: List<CandidateRecord>,
    ): List<ComparisonResult> {
        val simAfisCandidates = candidates.map { it.toSimAfisPerson() }

        println("Matching ${simAfisCandidates.size} candidates using all ${jniLibAfis.getNbCores()} cores")

        val results = jniLibAfis.identify(
            probe.toSimAfisPerson(),
            simAfisCandidates,
            jniLibAfis.getNbCores(),
        )

        return results.zip(simAfisCandidates).map { (score, candidate) ->
            ComparisonResult(candidate.guid, score)
        }
    }

    private fun CandidateRecord.toSimAfisPerson(): SimAfisPerson =
        SimAfisPerson(subjectId, references.flatMap { it.templates }.map { it.toSimAfisFingerprint() })

    private fun BiometricTemplate.toSimAfisFingerprint(): SimAfisFingerprint =
        SimAfisFingerprint(identifier.toSimAfisFingerIdentifier(), template)

    private fun List<BiometricTemplateCapture>.toSimAfisPerson(): SimAfisPerson = SimAfisPerson("", map { it.toSimAfisFingerprint() })

    private fun BiometricTemplateCapture.toSimAfisFingerprint(): SimAfisFingerprint =
        SimAfisFingerprint(identifier.toSimAfisFingerIdentifier(), template)

    @ExcludedFromGeneratedTestCoverageReports(reason = "This is just a mapping function")
    private fun TemplateIdentifier.toSimAfisFingerIdentifier(): SimAfisFingerIdentifier = when (this) {
        TemplateIdentifier.RIGHT_5TH_FINGER -> SimAfisFingerIdentifier.RIGHT_5TH_FINGER
        TemplateIdentifier.RIGHT_4TH_FINGER -> SimAfisFingerIdentifier.RIGHT_4TH_FINGER
        TemplateIdentifier.RIGHT_3RD_FINGER -> SimAfisFingerIdentifier.RIGHT_3RD_FINGER
        TemplateIdentifier.RIGHT_INDEX_FINGER -> SimAfisFingerIdentifier.RIGHT_INDEX_FINGER
        TemplateIdentifier.RIGHT_THUMB -> SimAfisFingerIdentifier.RIGHT_THUMB
        TemplateIdentifier.LEFT_THUMB -> SimAfisFingerIdentifier.LEFT_THUMB
        TemplateIdentifier.LEFT_INDEX_FINGER -> SimAfisFingerIdentifier.LEFT_INDEX_FINGER
        TemplateIdentifier.LEFT_3RD_FINGER -> SimAfisFingerIdentifier.LEFT_3RD_FINGER
        TemplateIdentifier.LEFT_4TH_FINGER -> SimAfisFingerIdentifier.LEFT_4TH_FINGER
        TemplateIdentifier.LEFT_5TH_FINGER -> SimAfisFingerIdentifier.LEFT_5TH_FINGER
        TemplateIdentifier.NONE -> throw IllegalArgumentException("Must be a finger sample identifier")
    }

    private fun crossFingerMatch(
        probe: List<BiometricTemplateCapture>,
        candidates: List<CandidateRecord>,
    ) = candidates.map { crossFingerMatching(probe, it, jniLibAfis) }

    /**
     * This method gets the matching score by:
     * - Getting the maximum matching score for each probe finger template with all candidate finger templates
     * - The overall score is the average of the individual finger match scores
     * @param probe
     * @param candidate
     * @return MatchResult
     */
    private fun crossFingerMatching(
        probe: List<BiometricTemplateCapture>,
        candidate: CandidateRecord,
        jniLibAfis: JNILibAfisInterface,
    ): ComparisonResult {
        // Fingers used in matching
        val fingers = probe.fingerprintsTemplates
        // Sum of maximum matching score for each finger
        val total = fingers
            .sumOf { probeTemplate ->
                candidate.fingerprintsTemplates
                    .maxOf { candidateTemplate ->
                        jniLibAfis.verify(
                            probeTemplate,
                            candidateTemplate,
                        )
                    }.toDouble()
            }
        // Matching score  = total/number of fingers
        return ComparisonResult(candidate.subjectId, getOverallScore(total, fingers.size))
    }

    private fun getOverallScore(
        total: Double,
        fingers: Int,
    ) = if (fingers == 0) {
        0.toFloat()
    } else {
        (total / fingers).toFloat()
    }

    companion object {
        const val SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT = "ISO_19794_2"
    }
}

val List<BiometricTemplateCapture>.fingerprintsTemplates
    get() = map { it.template.toByteBuffer() }

val CandidateRecord.fingerprintsTemplates
    get() = references.flatMap { it.templates }.map { it.template.toByteBuffer() }

private fun ByteArray.toByteBuffer(): ByteBuffer = ByteBuffer.allocateDirect(size).put(this)

fun BiometricReferenceCapture.templateFormatNotSupportedBySimAfisMatcher(): Boolean =
    format != SimAfisMatcher.SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT
