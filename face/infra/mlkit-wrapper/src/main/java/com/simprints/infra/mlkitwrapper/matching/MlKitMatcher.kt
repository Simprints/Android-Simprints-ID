package com.simprints.infra.mlkitwrapper.matching

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.tools.extentions.toFloats
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.infra.mlkitwrapper.MlKitModelContainer
import com.simprints.infra.mlkitwrapper.tools.cosineSimilarity
import javax.inject.Inject

class MlKitMatcher @Inject constructor(
    private val container: MlKitModelContainer,
) : FaceMatcher() {

    override val matcherName
        get() = container.matcher

    override val supportedTemplateFormat: String
        get() = container.templateFormat


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
