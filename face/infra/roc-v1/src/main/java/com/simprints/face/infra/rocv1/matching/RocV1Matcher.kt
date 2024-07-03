package com.simprints.face.infra.rocv1.matching

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.rocv1.detection.RocV1Detector.Companion.RANK_ONE_TEMPLATE_FORMAT_1_23
import io.rankone.rocsdk.embedded.roc
import io.rankone.rocsdk.embedded.rocConstants.ROC_FAST_FV_SIZE
import javax.inject.Inject

class RocV1Matcher @Inject constructor() : FaceMatcher() {
    override val matcherName
        get() = "RANK_ONE"

    override val supportedTemplateFormat
        get() = RANK_ONE_TEMPLATE_FORMAT_1_23

    // Ignore this method from test coverage calculations
    // because it uses jni native code which is hard to test
    @ExcludedFromGeneratedTestCoverageReports(
        reason = "This function uses roc class that has native functions and can't be mocked"
    )
    override suspend fun getComparisonScore(probe: ByteArray, matchAgainst: ByteArray): Float {
        val probeTemplate = roc.new_uint8_t_array(ROC_FAST_FV_SIZE.toInt())
        roc.memmove(roc.roc_cast(probeTemplate), probe)

        val matchTemplate = roc.new_uint8_t_array(ROC_FAST_FV_SIZE.toInt())
        roc.memmove(roc.roc_cast(matchTemplate), matchAgainst)

        val similarity = roc.roc_embedded_compare_templates(
            probeTemplate,
            ROC_FAST_FV_SIZE,
            matchTemplate,
            ROC_FAST_FV_SIZE
        )

        roc.delete_uint8_t_array(probeTemplate)
        roc.delete_uint8_t_array(matchTemplate)

        return (similarity * 100)
    }

}
