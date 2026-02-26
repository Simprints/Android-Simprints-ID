package com.simprints.face.infra.rocv1.matching

import com.simprints.biometrics.polyprotect.AuxData
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import io.rankone.rocsdk.embedded.SWIGTYPE_p_unsigned_char
import io.rankone.rocsdk.embedded.roc
import io.rankone.rocsdk.embedded.rocConstants.ROC_FAST_FV_SIZE

@ExcludedFromGeneratedTestCoverageReports(
    reason = "This function uses roc class that has native functions and can't be mocked",
)
class RocV1Matcher(
    override val probeReference: BiometricReferenceCapture,
) : FaceMatcher(probeReference) {
    var nativeProbeTemplates: List<SWIGTYPE_p_unsigned_char> = probeReference.templates
        .map { it.template }
        .map { probe ->
            val probeTemplate: SWIGTYPE_p_unsigned_char = roc.new_uint8_t_array(ROC_FAST_FV_SIZE.toInt())
            roc.memmove(roc.roc_cast(probeTemplate), probe)
            probeTemplate
        }

    override suspend fun getHighestComparisonScoreForCandidate(
        candidate: CandidateRecord,
        probeAuxData: AuxData?, // TODO PoC - no-op in this class
    ): Float = nativeProbeTemplates
        .flatMap { probeTemplate ->
            candidate.references.flatMap { it.templates }.map { face ->
                getSimilarityScoreForCandidate(probeTemplate, face.template)
            }
        }.max()

    private fun getSimilarityScoreForCandidate(
        probeTemplate: SWIGTYPE_p_unsigned_char,
        candidateTemplate: ByteArray,
    ): Float {
        val matchTemplate = roc.new_uint8_t_array(ROC_FAST_FV_SIZE.toInt())
        roc.memmove(roc.roc_cast(matchTemplate), candidateTemplate)

        val similarity = roc.roc_embedded_compare_templates(
            probeTemplate,
            ROC_FAST_FV_SIZE,
            matchTemplate,
            ROC_FAST_FV_SIZE,
        )
        roc.delete_uint8_t_array(matchTemplate)

        return similarity * 100f
    }

    override fun close() {
        nativeProbeTemplates.forEach { roc.delete_uint8_t_array(it) }
    }
}
