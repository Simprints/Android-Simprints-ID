package com.simprints.face.infra.simface.matching

import com.simprints.face.infra.basebiosdk.matching.FaceIdentity
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.basebiosdk.matching.FaceSample
import com.simprints.simface.core.SimFace

class SimFaceMatcher(
    private val simFace: SimFace,
    override val probeSamples: List<FaceSample>,
) : FaceMatcher(probeSamples) {
    private val probeTemplates = probeSamples.map { it.template }

    override suspend fun getHighestComparisonScoreForCandidate(candidate: FaceIdentity): Float = probeTemplates
        .flatMap { probeTemplate ->
            candidate.faces.map { face ->
                val baseScore = simFace.verificationScore(probeTemplate, face.template)
                // TODO: remove the adjustment after we find out why the returned range is biased towards [0.5;1]
                (baseScore - 0.5).coerceAtLeast(0.0).toFloat() * 200f
            }
        }.max()

    override fun close() {
        // No-op
    }
}
