package com.simprints.infra.facenetwrapper.matching

import com.google.common.truth.Truth
import org.junit.Test

class FaceNetMatcherTest {

    // Dummy test to generate jacoco reports.
    @Test
    fun getMatcherName() {
        FaceNetMatcher().matcherName
        Truth.assertThat(FaceNetMatcher().matcherName).isEqualTo("RANK_ONE")
    }
}
