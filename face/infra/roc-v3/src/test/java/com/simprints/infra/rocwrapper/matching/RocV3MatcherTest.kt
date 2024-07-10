package com.simprints.infra.rocwrapper.matching

import com.google.common.truth.Truth
import com.simprints.face.infra.rocv3.matching.RocV3Matcher
import org.junit.Test

class RocV3MatcherTest {
    @Test
    fun getMatcherName() {
        RocV3Matcher().matcherName
        Truth.assertThat(RocV3Matcher().matcherName).isEqualTo("RANK_ONE")
    }
}
