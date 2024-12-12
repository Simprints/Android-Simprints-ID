package com.simprints.fingerprint.infra.biosdkimpl.matching

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier.LEFT_3RD_FINGER
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier.LEFT_4TH_FINGER
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier.LEFT_5TH_FINGER
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier.LEFT_INDEX_FINGER
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier.LEFT_THUMB
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier.RIGHT_3RD_FINGER
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier.RIGHT_4TH_FINGER
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier.RIGHT_5TH_FINGER
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier.RIGHT_INDEX_FINGER
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerIdentifier.RIGHT_THUMB
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.Fingerprint
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult
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
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        crossFingerComparison: Boolean,
    ): List<MatchResult> {
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
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
    ): List<MatchResult> {
        val simAfisCandidates = candidates.map { it.toSimAfisPerson() }

        println("Matching ${simAfisCandidates.size} candidates using all ${jniLibAfis.getNbCores()} cores")

        val results = jniLibAfis.identify(
            probe.toSimAfisPerson(),
            simAfisCandidates,
            jniLibAfis.getNbCores(),
        )

        return results.zip(simAfisCandidates).map { (score, candidate) ->
            MatchResult(candidate.guid, score)
        }
    }

    private fun FingerprintIdentity.toSimAfisPerson(): SimAfisPerson =
        SimAfisPerson(subjectId, fingerprints.map { it.toSimAfisFingerprint() })

    private fun Fingerprint.toSimAfisFingerprint(): SimAfisFingerprint = SimAfisFingerprint(fingerId.toSimAfisFingerIdentifier(), template)

    @ExcludedFromGeneratedTestCoverageReports(reason = "This is just a mapping function")
    private fun FingerIdentifier.toSimAfisFingerIdentifier(): SimAfisFingerIdentifier = when (this) {
        RIGHT_5TH_FINGER -> SimAfisFingerIdentifier.RIGHT_5TH_FINGER
        RIGHT_4TH_FINGER -> SimAfisFingerIdentifier.RIGHT_4TH_FINGER
        RIGHT_3RD_FINGER -> SimAfisFingerIdentifier.RIGHT_3RD_FINGER
        RIGHT_INDEX_FINGER -> SimAfisFingerIdentifier.RIGHT_INDEX_FINGER
        RIGHT_THUMB -> SimAfisFingerIdentifier.RIGHT_THUMB
        LEFT_THUMB -> SimAfisFingerIdentifier.LEFT_THUMB
        LEFT_INDEX_FINGER -> SimAfisFingerIdentifier.LEFT_INDEX_FINGER
        LEFT_3RD_FINGER -> SimAfisFingerIdentifier.LEFT_3RD_FINGER
        LEFT_4TH_FINGER -> SimAfisFingerIdentifier.LEFT_4TH_FINGER
        LEFT_5TH_FINGER -> SimAfisFingerIdentifier.LEFT_5TH_FINGER
    }

    private fun crossFingerMatch(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
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
        probe: FingerprintIdentity,
        candidate: FingerprintIdentity,
        jniLibAfis: JNILibAfisInterface,
    ): MatchResult {
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
        return MatchResult(candidate.subjectId, getOverallScore(total, fingers))
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

val FingerprintIdentity.fingerprintsTemplates
    get() = fingerprints.map { it.template.toByteBuffer() }

private fun ByteArray.toByteBuffer(): ByteBuffer = ByteBuffer.allocateDirect(size).put(this)

fun FingerprintIdentity.templateFormatNotSupportedBySimAfisMatcher(): Boolean =
    fingerprints.any { it.format != SimAfisMatcher.SIMAFIS_MATCHER_SUPPORTED_TEMPLATE_FORMAT }
