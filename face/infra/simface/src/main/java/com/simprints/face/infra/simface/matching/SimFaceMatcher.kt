package com.simprints.face.infra.simface.matching

import com.simprints.biometrics.simface.SimFace
import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.sample.Identity
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher

class SimFaceMatcher(
    private val simFace: SimFace,
    override val probeReference: BiometricReferenceCapture,
) : FaceMatcher(probeReference) {
    override suspend fun getHighestComparisonScoreForCandidate(candidate: Identity): Float = probeReference
        .templates
        .map { it.template.template }
        .flatMap { probeTemplate ->
            candidate.samples.map { face ->
                val baseScore = simFace.verificationScore(probeTemplate, face.template.template)
                // TODO: remove the adjustment after we find out why the returned range is biased towards [0.5;1]
                (baseScore - 0.5).coerceAtLeast(0.0).toFloat() * 200f
            }
        }.max()

    override fun close() {
        // No-op
    }
}
