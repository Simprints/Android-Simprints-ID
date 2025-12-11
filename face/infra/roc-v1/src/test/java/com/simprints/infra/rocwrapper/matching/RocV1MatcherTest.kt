package com.simprints.infra.rocwrapper.matching

import com.google.common.truth.*
import com.simprints.face.infra.rocv1.matching.RocV1Matcher
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class RocV1MatcherTest {
    // Dummy test to generate jacoco reports.
    @Test
    fun getMatcherName() {
        Truth.assertThat(RocV1Matcher(mockk { every { templates } returns emptyList() })).isNotNull()
    }
}
