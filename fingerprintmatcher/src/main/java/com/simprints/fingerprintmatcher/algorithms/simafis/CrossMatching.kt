package com.simprints.fingerprintmatcher.algorithms.simafis

import com.simprints.fingerprintmatcher.domain.FingerprintIdentity
import com.simprints.fingerprintmatcher.domain.MatchResult
import java.nio.ByteBuffer

/**
 * This method gets the matching score by:
 * - Getting the maximum matching score for each probe finger template with all candidate finger templates
 * - The overall score is the average of the individual finger match scores
 * @param probe
 * @param candidate
 * @return MatchResult
 */
fun crossFingerMatching(
    probe: FingerprintIdentity,
    candidate: FingerprintIdentity,
    jniLibAfis: JNILibAfisInterface
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
                        candidateTemplate
                    )
                }.toDouble()
        }
    // Matching score  = total/number of fingers
    return MatchResult(candidate.id, getOverallScore(total, fingers))
}


private fun getOverallScore(total: Double, fingers: Int) =
    if (fingers == 0) {
        0.toFloat()
    } else {
        (total / fingers).toFloat()
    }

val FingerprintIdentity.fingerprintsTemplates
    get() = fingerprints.map { it.template.toByteBuffer() }

private fun ByteArray.toByteBuffer(): ByteBuffer =
    ByteBuffer.allocateDirect(size).put(this)
