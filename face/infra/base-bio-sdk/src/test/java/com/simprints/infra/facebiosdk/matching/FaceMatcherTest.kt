package com.simprints.infra.facebiosdk.matching

import com.google.common.truth.Truth.assertThat
import com.simprints.face.infra.basebiosdk.matching.FaceIdentity
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.basebiosdk.matching.FaceSample
import io.mockk.coEvery
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.UUID
import kotlin.random.Random

class FaceMatcherTest {
    private val faceMatcher = spyk<FaceMatcher>()
    private val candidate1 = getFaceIdentity(2)
    private val probes = generateSequenceN(2) { getFaceSample() }.toList()

    @Test
    fun `Get highest score for a candidate`() = runTest {
        coEvery { faceMatcher.getComparisonScore(any(), any()) } returnsMany listOf(
            0.1f,
            0.2f,
            0.3f,
            0.4f
        )

        val score = faceMatcher.getHighestComparisonScoreForCandidate(probes, candidate1)

        assertThat(score).isEqualTo(0.4f)
    }

    private fun getFaceIdentity(numFaces: Int): FaceIdentity =
        FaceIdentity(
            UUID.randomUUID().toString(),
            generateSequenceN(numFaces) { getFaceSample() }.toList()
        )

    private fun getFaceSample(): FaceSample =
        FaceSample(UUID.randomUUID().toString(), Random.nextBytes(20))

    private fun <T : Any> generateSequenceN(n: Int, f: () -> T) = generateSequence(f).take(n)
}
