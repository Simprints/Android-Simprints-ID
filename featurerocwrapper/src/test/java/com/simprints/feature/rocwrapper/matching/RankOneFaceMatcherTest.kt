package com.simprints.feature.rocwrapper.matching

import com.google.common.truth.Truth
import org.junit.Test

class RankOneFaceMatcherTest {

    // Dummy test to generate jacoco reports.
    @Test
    fun getMatcherName() {
        RankOneFaceMatcher().matcherName
        Truth.assertThat(RankOneFaceMatcher().matcherName).isEqualTo("RANK_ONE")
    }
}
