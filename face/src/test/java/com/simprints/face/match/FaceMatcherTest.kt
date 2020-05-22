package com.simprints.face.match

import com.google.common.truth.Truth.assertThat
import com.simprints.face.PeopleGenerator
import com.simprints.face.match.mock.MockFaceMatcher
import com.simprints.id.tools.utils.generateSequenceN
import io.mockk.coEvery
import io.mockk.spyk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class FaceMatcherTest {
    private val faceMatcher = spyk(MockFaceMatcher())
    private val candidate1 = PeopleGenerator.getFaceIdentity(2)
    private val probes = generateSequenceN(2) { PeopleGenerator.getFaceSample() }.toList()

    @Test
    fun `Get highest score for a candidate`() = runBlockingTest {
        coEvery { faceMatcher.getComparisonScore(any(), any()) } returnsMany listOf(
            0.1f,
            0.2f,
            0.3f,
            0.4f
        )

        val score = faceMatcher.getHighestComparisonScoreForCandidate(probes, candidate1)

        assertThat(score).isEqualTo(0.4f)
    }
}
