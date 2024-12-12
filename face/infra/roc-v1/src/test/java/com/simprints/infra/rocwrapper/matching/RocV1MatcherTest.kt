package com.simprints.infra.rocwrapper.matching

import com.google.common.truth.Truth
import com.simprints.face.infra.rocv1.matching.RocV1Matcher
import org.junit.Test

class RocV1MatcherTest {
    // Dummy test to generate jacoco reports.
    @Test
    fun getMatcherName() {
        RocV1Matcher().matcherName
        Truth.assertThat(RocV1Matcher().matcherName).isEqualTo("RANK_ONE")
    }
}
