package com.simprints.matcher.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.face.FaceSample
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.store.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.store.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
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
    lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    lateinit var resolveFaceBioSdk: ResolveFaceBioSdkUseCase

    @MockK
    lateinit var faceMatcher: FaceMatcher

    @MockK
    lateinit var createRangesUseCase: CreateRangesUseCase

    private lateinit var useCase: FaceMatcherUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        coEvery { resolveFaceBioSdk().matcher } returns faceMatcher
        useCase = FaceMatcherUseCase(
            enrolmentRecordRepository,
            resolveFaceBioSdk,
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
                queryForCandidates = SubjectQuery(),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
        )

        coVerify(exactly = 0) { faceMatcher.getHighestComparisonScoreForCandidate(any(), any()) }
    }

    @Test
    fun `Skips matching if there are no candidates`() = runTest {
        coEvery { enrolmentRecordRepository.count(any()) } returns 0

        useCase.invoke(
            MatchParams(
                probeFaceSamples = listOf(
                    MatchParams.FaceSample("faceId", byteArrayOf(1, 2, 3))
                ),
                flowType = FlowType.VERIFY,
                queryForCandidates = SubjectQuery(),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
        )

        coVerify(exactly = 0) { faceMatcher.getHighestComparisonScoreForCandidate(any(), any()) }
    }


    @Test
    fun `Correctly calls SDK matcher`() = runTest {
        coEvery { enrolmentRecordRepository.count(any(), any()) } returns 100
        coEvery { createRangesUseCase(any()) } returns listOf(0..99)
        coEvery { enrolmentRecordRepository.loadFaceIdentities(any(), any(), any()) } returns listOf(
            FaceIdentity(
                "subjectId",
                listOf(FaceSample(byteArrayOf(1, 2, 3), "format", "faceTemplate"))
            )
        )
        coEvery { faceMatcher.getHighestComparisonScoreForCandidate(any(), any()) } returns 42f

        var onLoadingCalled = false

        val result = useCase.invoke(
            matchParams = MatchParams(
                probeFaceSamples = listOf(
                    MatchParams.FaceSample("faceId", byteArrayOf(1, 2, 3))
                ),
                flowType = FlowType.VERIFY,
                queryForCandidates = SubjectQuery(),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
            onLoadingCandidates = { onLoadingCalled = true },
        )

        coVerify { faceMatcher.getHighestComparisonScoreForCandidate(any(), any()) }

        assertThat(onLoadingCalled).isTrue()

        assertThat(result.matchResultItems.first().subjectId).isEqualTo("subjectId")
        assertThat(result.matchResultItems.first().confidence).isEqualTo(42f)
    }
}
