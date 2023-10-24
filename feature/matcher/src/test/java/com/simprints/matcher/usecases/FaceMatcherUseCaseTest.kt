package com.simprints.matcher.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.FlowProvider
import com.simprints.core.domain.face.FaceSample
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.facebiosdk.matching.FaceMatcher
import com.simprints.matcher.MatchParams
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FaceMatcherUseCaseTest {

    @MockK
    lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @MockK
    lateinit var faceMatcher: FaceMatcher

    private lateinit var useCase: FaceMatcherUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = FaceMatcherUseCase(enrolmentRecordManager, faceMatcher)
    }

    @Test
    fun `Skips matching if there are no probes`() = runTest {
        coEvery { enrolmentRecordManager.loadFaceIdentities(any()) } returns flowOf(
            FaceIdentity(
                "subjectId",
                listOf(FaceSample(byteArrayOf(1, 2, 3), "format", "faceTemplate"))
            )
        )
        coEvery { faceMatcher.getHighestComparisonScoreForCandidate(any(), any()) } returns 1f

        useCase.invoke(
            MatchParams(
                flowType = FlowProvider.FlowType.VERIFY,
                queryForCandidates = SubjectQuery()
            ),
        )

        coVerify(exactly = 0) { faceMatcher.getHighestComparisonScoreForCandidate(any(), any()) }
    }

    @Test
    fun `Correctly calls SDK matcher`() = runTest {
        coEvery { enrolmentRecordManager.loadFaceIdentities(any()) } returns flowOf(
            FaceIdentity(
                "subjectId",
                listOf(FaceSample(byteArrayOf(1, 2, 3), "format", "faceTemplate"))
            )
        )
        coEvery { faceMatcher.getHighestComparisonScoreForCandidate(any(), any()) } returns 42f

        var onLoadingCalled = false
        var onMatchingCalled = false

        val results = useCase.invoke(
            MatchParams(
                probeFaceSamples = listOf(
                    MatchParams.FaceSample("faceId", byteArrayOf(1, 2, 3))
                ),
                flowType = FlowProvider.FlowType.VERIFY,
                queryForCandidates = SubjectQuery()
            ),
            { onLoadingCalled = true },
            { onMatchingCalled = true }
        )

        coVerify { faceMatcher.getHighestComparisonScoreForCandidate(any(), any()) }

        assertThat(onLoadingCalled).isTrue()
        assertThat(onMatchingCalled).isTrue()

        assertThat(results.first().subjectId).isEqualTo("subjectId")
        assertThat(results.first().confidence).isEqualTo(42f)
    }
}
