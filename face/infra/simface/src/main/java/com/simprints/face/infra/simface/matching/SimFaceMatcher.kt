package com.simprints.face.infra.simface.matching

import com.simprints.biometrics.polyprotect.AuxData
import com.simprints.biometrics.simface.SimFace
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.infra.protection.TemplateProtection

class SimFaceMatcher(
    private val simFace: SimFace,
    private val templateProtection: TemplateProtection,
    override val probeReference: BiometricReferenceCapture,
) : FaceMatcher(probeReference) {
    override suspend fun getHighestComparisonScoreForCandidate(
        candidate: CandidateRecord,
        probeAuxData: AuxData?,
    ): Float = probeReference
        .templates
        .map {
            if (probeAuxData == null) {
                it.template
            } else {
                // TODO PoC - a very inefficient 1:N implementation with protected templates
                templateProtection.encodeTemplate(
                    template = it.template,
                    auxData = probeAuxData,
                )
            }
        }.flatMap { probeTemplate ->
            candidate.references.flatMap { it.templates }.map { face ->
                val baseScore = simFace.verificationScore(probeTemplate, face.template)
                // TODO: remove the adjustment after we find out why the returned range is biased towards [0.5;1]
                (baseScore - 0.5).coerceAtLeast(0.0).toFloat() * 200f
            }
        }.max()

    override fun close() {
        // No-op
    }
}
