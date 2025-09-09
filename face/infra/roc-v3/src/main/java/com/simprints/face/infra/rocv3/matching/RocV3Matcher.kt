package com.simprints.face.infra.rocv3.matching

import ai.roc.rocsdk.embedded.SWIGTYPE_p_unsigned_char
import ai.roc.rocsdk.embedded.roc
import ai.roc.rocsdk.embedded.rocConstants.ROC_FACE_FAST_FV_SIZE
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.Identity
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher

@ExcludedFromGeneratedTestCoverageReports(
    reason = "This function uses roc class that has native functions and can't be mocked",
)
class RocV3Matcher(
    override val probeSamples: List<CaptureSample>,
) : FaceMatcher(probeSamples) {
    var probeTemplates: List<SWIGTYPE_p_unsigned_char> = probeSamples.mapIndexed { i, probe ->
        val probeTemplate: SWIGTYPE_p_unsigned_char =
            roc.new_uint8_t_array(ROC_FACE_FAST_FV_SIZE.toInt())
        roc.memmove(roc.roc_cast(probeTemplate), probe.template)
        probeTemplate
    }

    override suspend fun getHighestComparisonScoreForCandidate(candidate: Identity): Float = probeTemplates
        .flatMap { probeTemplate ->
            candidate.samples.map { face ->
                getSimilarityScoreForCandidate(probeTemplate, face.template)
            }
        }.max()

    private fun getSimilarityScoreForCandidate(
        probeTemplate: SWIGTYPE_p_unsigned_char,
        candidateTemplate: ByteArray,
    ): Float {
        val matchTemplate = roc.new_uint8_t_array(ROC_FACE_FAST_FV_SIZE.toInt())
        roc.memmove(roc.roc_cast(matchTemplate), candidateTemplate)

        val similarity = roc.roc_embedded_compare_templates(
            probeTemplate,
            ROC_FACE_FAST_FV_SIZE,
            matchTemplate,
            ROC_FACE_FAST_FV_SIZE,
        )
        roc.delete_uint8_t_array(matchTemplate)

        return similarity * 100f
    }

    override fun close() {
        probeTemplates.forEach { roc.delete_uint8_t_array(it) }
    }
}
