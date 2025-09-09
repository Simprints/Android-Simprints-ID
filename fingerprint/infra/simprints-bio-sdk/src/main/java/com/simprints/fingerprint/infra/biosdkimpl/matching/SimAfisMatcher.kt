package com.simprints.fingerprint.infra.biosdkimpl.matching

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.Identity
import com.simprints.core.domain.sample.MatchConfidence
import com.simprints.core.domain.sample.Sample
import com.simprints.core.domain.sample.SampleIdentifier
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
        probe: List<CaptureSample>,
        candidates: List<Identity>,
        crossFingerComparison: Boolean,
    ): List<MatchConfidence> {
        // if probe template format is not supported by SimAfisMatcher, return empty list
        if (probe.templateFormatNotSupportedBySimAfisMatcher()) {
            return emptyList()
        }
        return if (crossFingerComparison) {
            crossFingerMatch(probe, candidates)
        } else {
            match(probe, candidates)
        }
    }

    private fun match(
        probe: List<CaptureSample>,
        candidates: List<Identity>,
    ): List<MatchConfidence> {
        val simAfisCandidates = candidates.map { it.toSimAfisPerson() }

        println("Matching ${simAfisCandidates.size} candidates using all ${jniLibAfis.getNbCores()} cores")

        val results = jniLibAfis.identify(
            probe.toSimAfisPerson(),
            simAfisCandidates,
            jniLibAfis.getNbCores(),
        )

        return results.zip(simAfisCandidates).map { (score, candidate) ->
            MatchConfidence(candidate.guid, score)
        }
    }

    private fun Identity.toSimAfisPerson(): SimAfisPerson = SimAfisPerson(subjectId, samples.map { it.toSimAfisFingerprint() })

    private fun Sample.toSimAfisFingerprint(): SimAfisFingerprint = SimAfisFingerprint(identifier.toSimAfisFingerIdentifier(), template)

    private fun List<CaptureSample>.toSimAfisPerson(): SimAfisPerson = SimAfisPerson("", map { it.toSimAfisFingerprint() })

    private fun CaptureSample.toSimAfisFingerprint(): SimAfisFingerprint =
        SimAfisFingerprint(identifier.toSimAfisFingerIdentifier(), template)

    @ExcludedFromGeneratedTestCoverageReports(reason = "This is just a mapping function")
    private fun SampleIdentifier.toSimAfisFingerIdentifier(): SimAfisFingerIdentifier = when (this) {
        SampleIdentifier.RIGHT_5TH_FINGER -> SimAfisFingerIdentifier.RIGHT_5TH_FINGER
        SampleIdentifier.RIGHT_4TH_FINGER -> SimAfisFingerIdentifier.RIGHT_4TH_FINGER
        SampleIdentifier.RIGHT_3RD_FINGER -> SimAfisFingerIdentifier.RIGHT_3RD_FINGER
        SampleIdentifier.RIGHT_INDEX_FINGER -> SimAfisFingerIdentifier.RIGHT_INDEX_FINGER
        SampleIdentifier.RIGHT_THUMB -> SimAfisFingerIdentifier.RIGHT_THUMB
        SampleIdentifier.LEFT_THUMB -> SimAfisFingerIdentifier.LEFT_THUMB
        SampleIdentifier.LEFT_INDEX_FINGER -> SimAfisFingerIdentifier.LEFT_INDEX_FINGER
        SampleIdentifier.LEFT_3RD_FINGER -> SimAfisFingerIdentifier.LEFT_3RD_FINGER
        SampleIdentifier.LEFT_4TH_FINGER -> SimAfisFingerIdentifier.LEFT_4TH_FINGER
        SampleIdentifier.LEFT_5TH_FINGER -> SimAfisFingerIdentifier.LEFT_5TH_FINGER
        SampleIdentifier.NONE -> throw IllegalArgumentException("Must be a finger sample identifier")
    }

    private fun crossFingerMatch(
        probe: List<CaptureSample>,
        candidates: List<Identity>,
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
        probe: List<CaptureSample>,
        candidate: Identity,
        jniLibAfis: JNILibAfisInterface,
    ): MatchConfidence {
        // Number of fingers used in matching
        val fingers = probe.fingerprintsTemplates.size
        // Sum of maximum matching score for each finger
        val total = probe.fingerprintsTemplates
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
        return MatchConfidence(candidate.subjectId, getOverallScore(total, fingers))
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

val List<CaptureSample>.fingerprintsTemplates
    get() = map { it.template.toByteBuffer() }

val Identity.fingerprintsTemplates
    get() = samples.map { it.template.toByteBuffer() }

private fun ByteArray.toByteBuffer(): ByteBuffer = ByteBuffer.allocateDirect(size).put(this)

fun List<CaptureSample>.templateFormatNotSupportedBySimAfisMatcher(): Boolean =
    any { it.format != SimAfisMatcher.SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT }
