package com.simprints.matcher.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.face.FaceSample
import com.simprints.infra.enrolment.records.store.SubjectRepository
import com.simprints.infra.enrolment.records.store.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.facebiosdk.matching.FaceMatcher
import com.simprints.matcher.MatchParams
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class FaceMatcherUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var subjectRepository: SubjectRepository

    @MockK
    lateinit var faceMatcher: FaceMatcher

    @MockK
    lateinit var createRangesUseCase: CreateRangesUseCase

    private lateinit var useCase: FaceMatcherUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = FaceMatcherUseCase(
            subjectRepository,
            faceMatcher,
            createRangesUseCase,
            testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `Skips matching if there are no probes`() = runTest {
        coEvery { faceMatcher.getHighestComparisonScoreForCandidate(any(), any()) } returns 1f

        useCase.invoke(
            MatchParams(
                flowType = FlowType.VERIFY,
                queryForCandidates = SubjectQuery()
            ),
        )

        coVerify(exactly = 0) { faceMatcher.getHighestComparisonScoreForCandidate(any(), any()) }
    }

    @Test
    fun `Skips matching if there are no candidates`() = runTest {
        coEvery { subjectRepository.count(any()) } returns 0

        useCase.invoke(
            MatchParams(
                probeFaceSamples = listOf(
                    MatchParams.FaceSample("faceId", byteArrayOf(1, 2, 3))
                ),
                flowType = FlowType.VERIFY,
                queryForCandidates = SubjectQuery()
            ),
        )

        coVerify(exactly = 0) { faceMatcher.getHighestComparisonScoreForCandidate(any(), any()) }
    }


    @Test
    fun `Correctly calls SDK matcher`() = runTest {
        coEvery { subjectRepository.count(any()) } returns 100
        coEvery { createRangesUseCase(any()) } returns listOf(0..99)
        coEvery { subjectRepository.loadFaceIdentities(any(), any()) } returns listOf(
            FaceIdentity(
                "subjectId",
                listOf(FaceSample(byteArrayOf(1, 2, 3), "format", "faceTemplate"))
            )
        )
        coEvery { faceMatcher.getHighestComparisonScoreForCandidate(any(), any()) } returns 42f

        var onLoadingCalled = false

        val results = useCase.invoke(
            matchParams = MatchParams(
                probeFaceSamples = listOf(
                    MatchParams.FaceSample("faceId", byteArrayOf(1, 2, 3))
                ),
                flowType = FlowType.VERIFY,
                queryForCandidates = SubjectQuery()
            ),
            onLoadingCandidates = { onLoadingCalled = true },
        )

        coVerify { faceMatcher.getHighestComparisonScoreForCandidate(any(), any()) }

        assertThat(onLoadingCalled).isTrue()

        assertThat(results.first.first().subjectId).isEqualTo("subjectId")
        assertThat(results.first.first().confidence).isEqualTo(42f)
    }
}
