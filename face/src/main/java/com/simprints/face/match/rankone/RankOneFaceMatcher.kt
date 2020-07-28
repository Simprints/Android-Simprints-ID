package com.simprints.face.match.rankone

import com.simprints.face.match.FaceMatcher
import io.rankone.rocsdk.embedded.roc
import io.rankone.rocsdk.embedded.rocConstants.ROC_FR_FAST_FV_SIZE
import io.rankone.rocsdk.embedded.roc_algorithm_options.ROC_FR_FAST

class RankOneFaceMatcher : FaceMatcher() {
    override suspend fun getComparisonScore(probe: ByteArray, matchAgainst: ByteArray): Float {
        val probeTemplate = roc.new_uint8_t_array(ROC_FR_FAST_FV_SIZE.toInt())
        roc.memmove(roc.roc_cast(probeTemplate), probe)

        val matchTemplate = roc.new_uint8_t_array(ROC_FR_FAST_FV_SIZE.toInt())
        roc.memmove(roc.roc_cast(matchTemplate), matchAgainst)

        val similarity = roc.roc_embedded_compare_templates(
            ROC_FR_FAST.toLong(),
            probeTemplate,
            ROC_FR_FAST_FV_SIZE,
            matchTemplate,
            ROC_FR_FAST_FV_SIZE
        )

        roc.delete_uint8_t_array(probeTemplate)
        roc.delete_uint8_t_array(matchTemplate)

        return (similarity * 100)
    }

}
