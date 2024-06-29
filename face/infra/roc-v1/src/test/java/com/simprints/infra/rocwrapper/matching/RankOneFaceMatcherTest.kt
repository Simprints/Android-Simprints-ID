package com.simprints.infra.rocwrapper.matching

import com.google.common.truth.Truth
import com.simprints.face.infra.rocv1.matching.RankOneFaceMatcher
import org.junit.Test

class RankOneFaceMatcherTest {

    // Dummy test to generate jacoco reports.
    @Test
    fun getMatcherName() {
        RankOneFaceMatcher().matcherName
        Truth.assertThat(RankOneFaceMatcher().matcherName).isEqualTo("RANK_ONE")
    }
}
