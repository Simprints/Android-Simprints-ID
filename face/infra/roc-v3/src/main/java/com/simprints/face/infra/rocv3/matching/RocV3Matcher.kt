package com.simprints.face.infra.rocv3.matching

import ai.roc.rocsdk.embedded.roc
import ai.roc.rocsdk.embedded.rocConstants.ROC_FACE_FAST_FV_SIZE
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.rocv3.detection.RocV3Detector.Companion.RANK_ONE_TEMPLATE_FORMAT_3_1
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RocV3Matcher @Inject constructor() : FaceMatcher() {
    override val matcherName
        get() = "RANK_ONE"


    override val supportedTemplateFormat
        get() = RANK_ONE_TEMPLATE_FORMAT_3_1

    // Ignore this method from test coverage calculations
    // because it uses jni native code which is hard to test
    @ExcludedFromGeneratedTestCoverageReports(
        reason = "This function uses roc class that has native functions and can't be mocked"
    )
    override suspend fun getComparisonScore(probe: ByteArray, matchAgainst: ByteArray): Float {

        val probeTemplate = roc.new_uint8_t_array(ROC_FACE_FAST_FV_SIZE.toInt())
        roc.memmove(roc.roc_cast(probeTemplate), probe)

        val matchTemplate = roc.new_uint8_t_array(ROC_FACE_FAST_FV_SIZE.toInt())
        roc.memmove(roc.roc_cast(matchTemplate), matchAgainst)

        val similarity = roc.roc_embedded_compare_templates(
            probeTemplate,
            ROC_FACE_FAST_FV_SIZE,
            matchTemplate,
            ROC_FACE_FAST_FV_SIZE
        )

        roc.delete_uint8_t_array(probeTemplate)
        roc.delete_uint8_t_array(matchTemplate)

        return (similarity * 100)
    }

}
