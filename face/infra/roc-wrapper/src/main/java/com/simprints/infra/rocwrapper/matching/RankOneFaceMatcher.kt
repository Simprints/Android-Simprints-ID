package com.simprints.infra.rocwrapper.matching

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.facebiosdk.matching.FaceMatcher
import javax.inject.Inject

class RankOneFaceMatcher @Inject constructor() : FaceMatcher() {
    override val matcherName
        get() = "RANK_ONE"


    // Ignore this method from test coverage calculations
    // because it uses jni native code which is hard to test
    @ExcludedFromGeneratedTestCoverageReports(
        reason = "This function uses roc class that has native functions and can't be mocked"
    )
    override suspend fun getComparisonScore(probe: ByteArray, matchAgainst: ByteArray): Float {


        return 0f
    }

}
