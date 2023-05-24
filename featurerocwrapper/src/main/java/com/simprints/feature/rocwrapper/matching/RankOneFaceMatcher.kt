package com.simprints.feature.rocwrapper.matching

import com.simprints.core.Generated
import com.simprints.infra.facebiosdk.matching.FaceMatcher
import io.rankone.rocsdk.embedded.roc
import io.rankone.rocsdk.embedded.rocConstants.ROC_FAST_FV_SIZE
import javax.inject.Inject

// Ignore this class from test coverage calculations
// because it uses jni native code which is hard to test
@Generated
class RankOneFaceMatcher @Inject constructor() : FaceMatcher() {
    override val matcherName
        get() = "RANK_ONE"

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
