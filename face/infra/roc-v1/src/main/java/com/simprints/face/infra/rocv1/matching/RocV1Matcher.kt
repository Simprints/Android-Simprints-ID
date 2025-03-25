package com.simprints.face.infra.rocv1.matching

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.face.infra.basebiosdk.matching.FaceIdentity
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.basebiosdk.matching.FaceSample
import io.rankone.rocsdk.embedded.SWIGTYPE_p_unsigned_char
import io.rankone.rocsdk.embedded.roc
import io.rankone.rocsdk.embedded.rocConstants.ROC_FAST_FV_SIZE

@ExcludedFromGeneratedTestCoverageReports(
    reason = "This function uses roc class that has native functions and can't be mocked",
)
class RocV1Matcher(
    override val probeSamples: List<FaceSample>
) : FaceMatcher(probeSamples) {

    var probeTemplates: List<SWIGTYPE_p_unsigned_char> = probeSamples.mapIndexed { i, probe ->
        val probeTemplate: SWIGTYPE_p_unsigned_char =
            roc.new_uint8_t_array(ROC_FAST_FV_SIZE.toInt())
        roc.memmove(roc.roc_cast(probeTemplate), probe.template)
        probeTemplate
    }

    override suspend fun getHighestComparisonScoreForCandidate(candidate: FaceIdentity): Float =
        probeTemplates.flatMap { probeTemplate ->
            candidate.faces.map { face ->
                getSimilarityScoreForCandidate(probeTemplate, face.template)
            }
        }.max()

    private fun getSimilarityScoreForCandidate(
        probeTemplate: SWIGTYPE_p_unsigned_char,
        candidateTemplate: ByteArray
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
        probeTemplates.forEach { roc.delete_uint8_t_array(it) }
    }
}
