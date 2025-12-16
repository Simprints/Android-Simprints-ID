package com.simprints.infra.matching.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.reference.BiometricReference
import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.reference.BiometricTemplateCapture
import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.core.domain.sample.ComparisonResult
import com.simprints.core.tools.time.TimeHelper
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
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
                    flowType = FlowType.VERIFY,
                    queryForCandidates = EnrolmentRecordQuery(),
                    bioSdk = FaceConfiguration.BioSdk.RANK_ONE,
                    biometricDataSource = BiometricDataSource.Simprints,
                    probeReference = BiometricReferenceCapture(
                        referenceId = "referenceId",
                        modality = Modality.FACE,
                        format = "format",
                        templates = emptyList(),
                    ),
                ),
                project,
            ).toList()

        coVerify(exactly = 0) { faceMatcher.getHighestComparisonScoreForCandidate(any()) }

        assertThat(results).containsExactly(
            MatcherUseCase.MatcherState.Success(
                comparisonResults = emptyList(),
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
                    probeReference = BiometricReferenceCapture(
                        referenceId = "referenceId",
                        modality = Modality.FACE,
                        format = "format",
                        templates = listOf(
                            BiometricTemplateCapture(
                                captureEventId = "faceId",
                                template = byteArrayOf(1, 2, 3),
                            ),
                        ),
                    ),
                    bioSdk = FaceConfiguration.BioSdk.RANK_ONE,
                    flowType = FlowType.VERIFY,
                    queryForCandidates = EnrolmentRecordQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                ),
                project,
            ).toList()

        coVerify(exactly = 0) { faceMatcher.getHighestComparisonScoreForCandidate(any()) }

        assertThat(results).containsExactly(
            MatcherUseCase.MatcherState.Success(
                comparisonResults = emptyList(),
                totalCandidates = 0,
                matcherName = "",
                matchBatches = emptyList(),
            ),
        )
    }

    @Test
    fun `Logs warning and returns empty success when wrong SDK type is provided`() = runTest {
        mockkObject(Simber)
        justRun { Simber.w(message = any<String>(), t = any<Throwable>(), tag = any<String>()) }

        val results = useCase
            .invoke(
                MatchParams(
                    probeReference = BiometricReferenceCapture(
                        referenceId = "referenceId",
                        modality = Modality.FACE,
                        format = "format",
                        templates = listOf(
                            BiometricTemplateCapture(
                                captureEventId = "faceId",
                                template = byteArrayOf(1, 2, 3),
                            ),
                        ),
                    ),
                    bioSdk = FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER, // Wrong SDK type
                    flowType = FlowType.VERIFY,
                    queryForCandidates = EnrolmentRecordQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                ),
                project,
            ).toList()

        verify {
            Simber.w(
                message = "Face SDK was not provided",
                t = ofType<IllegalArgumentException>(),
                tag = LoggingConstants.CrashReportTag.FACE_MATCHING,
            )
        }

        assertThat(results).containsExactly(
            MatcherUseCase.MatcherState.Success(
                comparisonResults = emptyList(),
                totalCandidates = 0,
                matcherName = "",
                matchBatches = emptyList(),
            ),
        )

        coVerify(exactly = 0) { faceMatcher.getHighestComparisonScoreForCandidate(any()) }
        unmockkObject(Simber)
    }

    @Test
    fun `Correctly calls SDK matcher`() = runTest {
        val totalCandidates = 1
        val faceCandidates = listOf(
            CandidateRecord(
                "subjectId",
                listOf(
                    BiometricReference(
                        templates = listOf(
                            BiometricTemplate(
                                template = byteArrayOf(1, 2, 3),
                            ),
                        ),
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
            enrolmentRecordRepository.loadCandidateRecords(any(), any(), any(), any(), any(), any())
        } answers {
            // Call the onCandidateLoaded callback (5th parameter)
            val onCandidateLoaded: suspend () -> Unit = arg(5)
            runBlocking {
                onCandidateLoaded()
            }

            // Return the face identities
            createTestChannel(faceCandidates)
        }
        coEvery { faceMatcher.getHighestComparisonScoreForCandidate(any()) } returns 42f

        val results = useCase
            .invoke(
                matchParams = MatchParams(
                    probeReference = BiometricReferenceCapture(
                        referenceId = "referenceId",
                        modality = Modality.FACE,
                        format = "format",
                        templates = listOf(
                            BiometricTemplateCapture(
                                captureEventId = "faceId",
                                template = byteArrayOf(1, 2, 3),
                            ),
                        ),
                    ),
                    bioSdk = FaceConfiguration.BioSdk.RANK_ONE,
                    flowType = FlowType.VERIFY,
                    queryForCandidates = EnrolmentRecordQuery(),
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
        assertThat(successState.comparisonResults).containsExactly(ComparisonResult("subjectId", 42f))
        assertThat(successState.totalCandidates).isEqualTo(totalCandidates)
        assertThat(successState.matcherName).isEqualTo("")

        // Verify only the size of matchBatches instead of exact content
        assertThat(successState.matchBatches).hasSize(1)
        assertThat(successState.matchBatches[0].count).isEqualTo(faceCandidates.size)
    }
}
