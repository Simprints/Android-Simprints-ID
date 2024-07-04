package com.simprints.infra.mlkitwrapper.matching

import com.google.common.truth.Truth
import org.junit.Test

class FaceNetMatcherTest {

    // Dummy test to generate jacoco reports.
    @Test
    fun getMatcherName() {
        MlKitMatcher().matcherName
        Truth.assertThat(MlKitMatcher().matcherName).isEqualTo("RANK_ONE")
    }
}
