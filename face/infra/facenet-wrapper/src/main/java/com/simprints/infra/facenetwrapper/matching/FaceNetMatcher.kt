package com.simprints.infra.facenetwrapper.matching

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.facebiosdk.matching.FaceMatcher
import com.simprints.infra.mlkitwrapper.tools.cosineSimilarity
import com.simprints.infra.mlkitwrapper.tools.toFloats
import javax.inject.Inject

class FaceNetMatcher @Inject constructor() : FaceMatcher() {
    override val matcherName
        get() = "RANK_ONE"


    // Ignore this method from test coverage calculations
    // because it uses jni native code which is hard to test
    @ExcludedFromGeneratedTestCoverageReports(
        reason = "This function uses roc class that has native functions and can't be mocked"
    )
    override suspend fun getComparisonScore(probe: ByteArray, matchAgainst: ByteArray): Float {
        val probeFloats = probe.toFloats()
        val matchAgainstFloats = matchAgainst.toFloats()
        //Template sizes do not match and from different providers return 0
        if (probeFloats.size != matchAgainstFloats.size) return 0f
        return (cosineSimilarity(probeFloats, matchAgainstFloats)) * 100f
    }


}
