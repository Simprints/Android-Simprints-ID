package com.simprints.face.infra.simface.matching

import com.google.common.truth.Truth.*
import com.simprints.face.infra.basebiosdk.matching.FaceIdentity
import com.simprints.face.infra.basebiosdk.matching.FaceSample
import com.simprints.simface.core.SimFace
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

// Dummy test to generate jacoco reports.
class SimFaceMatcherTest {
    @Test
    fun getMatcherName() {
        assertThat(SimFaceMatcher(mockk(relaxed = true), emptyList())).isNotNull()
    }

    @Test
    fun `comparison score in correct range`() = runTest {
        val expectedResults = mapOf(
            0.0 to 0.0f,
            0.1 to 0.0f,
            0.2 to 0.0f,
            0.3 to 0.0f,
            0.4 to 0.0f,
            0.5 to 0.0f,
            0.55 to 10f,
            0.60 to 20f,
            0.65 to 30f,
            0.70 to 40f,
            0.75 to 50f,
            0.80 to 60f,
            0.85 to 70f,
            0.90 to 80f,
            0.95 to 90f,
            1.00 to 100f,
        )

        expectedResults.forEach { (verificationScore, expectedScore) ->
            val simFace = mockk<SimFace> {
                every { verificationScore(any(), any()) } returns verificationScore
            }
            val matcher = SimFaceMatcher(
                simFace = simFace,
                probeSamples = listOf(FaceSample(faceId = "id", template = byteArrayOf(1))),
            )
            val result = matcher.getHighestComparisonScoreForCandidate(
                candidate = FaceIdentity(
                    subjectId = "id",
                    faces = listOf(FaceSample(faceId = "id", template = byteArrayOf(1))),
                ),
            )
            assertThat(result - expectedScore).isLessThan(0.0001f)
        }
    }

    @Test
    fun `returns only the highest score`() = runTest {
        val simFace = mockk<SimFace> {
            every { verificationScore(any(), any()) } returnsMany listOf(0.0, 0.9, 0.6)
        }
        val matcher = SimFaceMatcher(
            simFace = simFace,
            probeSamples = listOf(FaceSample(faceId = "id", template = byteArrayOf(1))),
        )
        val result = matcher.getHighestComparisonScoreForCandidate(
            candidate = FaceIdentity(
                subjectId = "id",
                faces = listOf(FaceSample(faceId = "id", template = byteArrayOf(1))),
            ),
        )
        assertThat(result - 80f).isLessThan(0.0001f)
    }
}
