package com.simprints.infra.rocwrapper.matching

import com.google.common.truth.Truth
import com.simprints.face.infra.rocv3.matching.RocV3Matcher
import org.junit.Test

// Dummy test to generate jacoco reports.
class RocV3MatcherTest {
    @Test
    fun getMatcherName() {
        Truth.assertThat(RocV3Matcher(emptyList())).isNotNull()
    }
}
