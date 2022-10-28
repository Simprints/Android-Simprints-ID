package com.simprints.fingerprintmatcher.algorithms.simafis

import com.simprints.fingerprintmatcher.algorithms.simafis.models.SimAfisFingerIdentifier
import com.simprints.fingerprintmatcher.algorithms.simafis.models.SimAfisFingerprint
import com.simprints.fingerprintmatcher.algorithms.simafis.models.SimAfisPerson
import com.simprints.fingerprintmatcher.domain.*
import com.simprints.fingerprintmatcher.domain.FingerIdentifier.*
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
internal class SimAfisMatcher(private val jniLibAfis: JNILibAfisInterface) {

    @Inject
    constructor() : this(JNILibAfis)

    fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        crossFingerComparison: Boolean
    ): List<MatchResult> {
        return if (crossFingerComparison) {
            crossFingerMatch(probe, candidates)
        } else {
            match(probe, candidates)
        }
    }

    private fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>
    ): List<MatchResult> {
        val simAfisCandidates = candidates.map { it.toSimAfisPerson() }

        println("Matching ${simAfisCandidates.size} candidates using all ${jniLibAfis.getNbCores()} cores")

        val results = jniLibAfis.identify(
            probe.toSimAfisPerson(),
            simAfisCandidates,
            jniLibAfis.getNbCores()
        )

        return results.zip(simAfisCandidates).map { (score, candidate) ->
            MatchResult(candidate.guid, score)
        }
    }

    private fun FingerprintIdentity.toSimAfisPerson(): SimAfisPerson =
        SimAfisPerson(id, fingerprints.map { it.toSimAfisFingerprint() })

    private fun Fingerprint.toSimAfisFingerprint(): SimAfisFingerprint {
        if (format != TemplateFormat.ISO_19794_2_2011)
            throw IllegalArgumentException("Attempting to use $format template format for SimAfisMatcher which only accepts ${TemplateFormat.ISO_19794_2_2011}")
        return SimAfisFingerprint(fingerId.toSimAfisFingerIdentifier(), template)
    }

    private fun FingerIdentifier.toSimAfisFingerIdentifier(): SimAfisFingerIdentifier =
        when (this) {
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
        candidates: List<FingerprintIdentity>
    ) = candidates.map { crossFingerMatching(probe, it, jniLibAfis) }

}
