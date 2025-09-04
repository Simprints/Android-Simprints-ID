package com.simprints.infra.matching.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.MatchConfidence
import com.simprints.core.domain.sample.Sample
import com.simprints.core.tools.time.TimeHelper
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.matching.MatchParams
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
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
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    lateinit var resolveFaceBioSdk: ResolveFaceBioSdkUseCase

    @MockK
    lateinit var faceMatcher: FaceMatcher

    @MockK
    lateinit var project: Project

    @MockK
    lateinit var createRangesUseCase: CreateRangesUseCase
    private lateinit var useCase: FaceMatcherUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        coEvery { resolveFaceBioSdk(any()).createMatcher(any()) } returns faceMatcher
        useCase = FaceMatcherUseCase(
            timeHelper,
            enrolmentRecordRepository,
            resolveFaceBioSdk,
            createRangesUseCase,
            testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `Skips matching if there are no probes`() = runTest {
        coEvery { faceMatcher.getHighestComparisonScoreForCandidate(any()) } returns 1f

        val results = useCase
            .invoke(
                MatchParams(
                    probeReferenceId = "referenceId",
                    flowType = FlowType.VERIFY,
                    queryForCandidates = SubjectQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                ),
                project,
            ).toList()

        coVerify(exactly = 0) { faceMatcher.getHighestComparisonScoreForCandidate(any()) }

        assertThat(results).containsExactly(
            MatcherUseCase.MatcherState.Success(
                matchResultItems = emptyList(),
                totalCandidates = 0,
                matcherName = "",
                matchBatches = emptyList(),
            ),
        )
    }

    @Test
    fun `Skips matching if there are no candidates`() = runTest {
        coEvery { enrolmentRecordRepository.count(any()) } returns 0

        val results = useCase
            .invoke(
                MatchParams(
                    probeReferenceId = "referenceId",
                    probeFaceSamples = listOf(
                        CaptureSample(
                            captureEventId = "faceId",
                            template = byteArrayOf(1, 2, 3),
                            modality = Modality.FACE,
                            format = "format",
                        ),
                    ),
                    faceSDK = FaceConfiguration.BioSdk.RANK_ONE,
                    flowType = FlowType.VERIFY,
                    queryForCandidates = SubjectQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                ),
                project,
            ).toList()

        coVerify(exactly = 0) { faceMatcher.getHighestComparisonScoreForCandidate(any()) }

        assertThat(results).containsExactly(
            MatcherUseCase.MatcherState.Success(
                matchResultItems = emptyList(),
                totalCandidates = 0,
                matcherName = "",
                matchBatches = emptyList(),
            ),
        )
    }

    @Test
    fun `Correctly calls SDK matcher`() = runTest {
        val totalCandidates = 1
        val faceIdentities = listOf(
            FaceIdentity(
                "subjectId",
                listOf(
                    Sample(
                        template = byteArrayOf(1, 2, 3),
                        format = "format",
                        referenceId = "faceTemplate",
                        modality = Modality.FACE,
                    ),
                ),
            ),
        )
        coEvery { enrolmentRecordRepository.count(any(), any()) } returns 1
        coEvery { createRangesUseCase(any()) } returns listOf(0..99)
        coEvery {
            enrolmentRecordRepository.loadFaceIdentities(any(), any(), any(), any(), any(), any())
        } answers {
            // Call the onCandidateLoaded callback (5th parameter)
            val onCandidateLoaded: suspend () -> Unit = arg(5)
            runBlocking {
                onCandidateLoaded()
            }

            // Return the face identities
            createTestChannel(faceIdentities)
        }
        coEvery { faceMatcher.getHighestComparisonScoreForCandidate(any()) } returns 42f

        val results = useCase
            .invoke(
                matchParams = MatchParams(
                    probeReferenceId = "referenceId",
                    probeFaceSamples = listOf(
                        CaptureSample(
                            captureEventId = "faceId",
                            template = byteArrayOf(1, 2, 3),
                            modality = Modality.FACE,
                            format = "format",
                        ),
                    ),
                    faceSDK = FaceConfiguration.BioSdk.RANK_ONE,
                    flowType = FlowType.VERIFY,
                    queryForCandidates = SubjectQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                ),
                project,
            ).toList()

        coVerify { faceMatcher.getHighestComparisonScoreForCandidate(any()) }

        // Check results with matchBatches verification by size
        assertThat(results.size).isEqualTo(3)
        assertThat(results[0]).isInstanceOf(MatcherUseCase.MatcherState.LoadingStarted::class.java)
        assertThat(results[1]).isInstanceOf(MatcherUseCase.MatcherState.CandidateLoaded::class.java)

        val successState = results[2] as MatcherUseCase.MatcherState.Success
        assertThat(successState.matchResultItems).containsExactly(MatchConfidence("subjectId", 42f))
        assertThat(successState.totalCandidates).isEqualTo(totalCandidates)
        assertThat(successState.matcherName).isEqualTo("")

        // Verify only the size of matchBatches instead of exact content
        assertThat(successState.matchBatches).hasSize(1)
        assertThat(successState.matchBatches[0].count).isEqualTo(faceIdentities.size)
    }
}
