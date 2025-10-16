package com.simprints.matcher.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.MatchConfidence
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.matching.MatchBatchInfo
import com.simprints.infra.matching.MatchParams
import com.simprints.infra.matching.MatchResult
import com.simprints.infra.matching.usecase.FaceMatcherUseCase
import com.simprints.infra.matching.usecase.FingerprintMatcherUseCase
import com.simprints.infra.matching.usecase.MatcherUseCase
import com.simprints.infra.matching.usecase.SaveMatchEventUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
internal class MatchViewModelTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @MockK
    lateinit var faceMatcherUseCase: FaceMatcherUseCase

    @MockK
    lateinit var fingerprintMatcherUseCase: FingerprintMatcherUseCase

    @MockK
    lateinit var saveMatchEvent: SaveMatchEventUseCase

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var configManager: ConfigManager

    private lateinit var cb1: CapturingSlot<(Int) -> Unit>
    private lateinit var viewModel: MatchViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        cb1 = slot()

        every { timeHelper.now() } returns Timestamp(0L)

        viewModel = MatchViewModel(
            faceMatcherUseCase,
            fingerprintMatcherUseCase,
            saveMatchEvent,
            authStore,
            configManager,
            timeHelper,
        )
    }

    @Test
    fun `when setup is called, then view model becomes initialized`() = runTest {
        val responseItems = listOf(
            MatchConfidence("1", 90f),
        )

        coEvery { faceMatcherUseCase.invoke(any(), any()) } returns flow {
            emit(MatcherUseCase.MatcherState.LoadingStarted(responseItems.size))
            emit(MatcherUseCase.MatcherState.CandidateLoaded)
            emit(
                MatcherUseCase.MatcherState.Success(
                    matchResultItems = responseItems,
                    totalCandidates = responseItems.size,
                    matcherName = "MatcherName",
                    matchBatches = emptyList(),
                ),
            )
        }
        coJustRun { saveMatchEvent.invoke(any(), any(), any(), any(), any(), any(), any()) }

        assertThat(viewModel.isInitialized).isFalse()

        viewModel.matchState.test()
        viewModel.setupMatch(
            MatchParams(
                probeReferenceId = "referenceId",
                probeFaceSamples = listOf(getFaceSample()),
                bioSdk = FaceConfiguration.BioSdk.RANK_ONE,
                flowType = FlowType.ENROL,
                queryForCandidates = mockk {},
                biometricDataSource = BiometricDataSource.Simprints,
            ),
        )

        assertThat(viewModel.isInitialized).isTrue()
    }

    @Test
    fun `Handles no permission call`() = runTest {
        val states = viewModel.matchState.test()
        viewModel.noPermission(true)
        assertThat(states.value()).isEqualTo(MatchViewModel.MatchState.NoPermission(true))
    }

    @Test
    fun `Handle face match request correctly`() = runTest {
        coEvery {
            configManager
                .getProjectConfiguration()
                .face
                ?.getSdkConfiguration(any())
                ?.decisionPolicy
        } returns DecisionPolicy(20, 35, 50)

        val responseItems = listOf(
            MatchConfidence("1", 90f),
            MatchConfidence("1", 80f),
            MatchConfidence("1", 55f),
            MatchConfidence("1", 40f),
            MatchConfidence("1", 36f),
            MatchConfidence("1", 20f),
            MatchConfidence("1", 10f),
        )
        val batches = listOf(
            MatchBatchInfo(
                loadingStartTime = Timestamp(0L),
                loadingEndTime = Timestamp(1000L),
                comparingStartTime = Timestamp(2000L),
                comparingEndTime = Timestamp(3000L),
                count = 3,
            ),
            MatchBatchInfo(
                loadingStartTime = Timestamp(4000L),
                loadingEndTime = Timestamp(5000L),
                comparingStartTime = Timestamp(6000L),
                comparingEndTime = Timestamp(7000L),
                count = 4,
            ),
        )

        coEvery { faceMatcherUseCase.invoke(any(), any()) } returns flow {
            emit(MatcherUseCase.MatcherState.LoadingStarted(responseItems.size))
            emit(MatcherUseCase.MatcherState.CandidateLoaded)
            emit(
                MatcherUseCase.MatcherState.Success(
                    matchResultItems = responseItems,
                    totalCandidates = responseItems.size,
                    matcherName = MATCHER_NAME,
                    matchBatches = batches,
                ),
            )
        }
        coJustRun { saveMatchEvent.invoke(any(), any(), any(), any(), any(), any(), any()) }

        val states = viewModel.matchState.test()
        viewModel.setupMatch(
            MatchParams(
                probeReferenceId = "referenceId",
                probeFaceSamples = listOf(getFaceSample()),
                bioSdk = FaceConfiguration.BioSdk.RANK_ONE,
                flowType = FlowType.ENROL,
                queryForCandidates = mockk {},
                biometricDataSource = BiometricDataSource.Simprints,
            ),
        )
        // Waiting for the ::delay in viewModel::setupMatch
        advanceUntilIdle()

        assertThat(states.valueHistory()).isEqualTo(
            listOf(
                MatchViewModel.MatchState.NotStarted,
                MatchViewModel.MatchState.LoadingCandidates(responseItems.size, 0),
                MatchViewModel.MatchState.LoadingCandidates(responseItems.size, 1),
                MatchViewModel.MatchState.Finished(7, 7, 3, 2, 1),
            ),
        )
        assertThat(viewModel.matchResponse.getOrAwaitValue().peekContent()).isEqualTo(
            MatchResult(responseItems, FaceConfiguration.BioSdk.RANK_ONE),
        )

        verify {
            saveMatchEvent.invoke(
                any(),
                any(),
                any(),
                eq(7),
                eq(MATCHER_NAME),
                any(),
                withArg { list -> assertThat(list.size).isEqualTo(batches.size) },
            )
        }
    }

    @Test
    fun `Handle fingerprint match request correctly`() = runTest {
        coEvery {
            configManager
                .getProjectConfiguration()
                .fingerprint
                ?.getSdkConfiguration(any())
                ?.decisionPolicy
        } returns DecisionPolicy(200, 350, 500)

        val responseItems = listOf(
            MatchConfidence("1", 900f),
            MatchConfidence("1", 800f),
            MatchConfidence("1", 550f),
            MatchConfidence("1", 400f),
            MatchConfidence("1", 360f),
            MatchConfidence("1", 200f),
            MatchConfidence("1", 100f),
        )
        val batches = listOf(
            MatchBatchInfo(
                loadingStartTime = Timestamp(0L),
                loadingEndTime = Timestamp(1000L),
                comparingStartTime = Timestamp(2000L),
                comparingEndTime = Timestamp(3000L),
                count = 3,
            ),
            MatchBatchInfo(
                loadingStartTime = Timestamp(4000L),
                loadingEndTime = Timestamp(5000L),
                comparingStartTime = Timestamp(6000L),
                comparingEndTime = Timestamp(7000L),
                count = 4,
            ),
        )

        coEvery { fingerprintMatcherUseCase.invoke(any(), any()) } returns flow {
            emit(MatcherUseCase.MatcherState.LoadingStarted(responseItems.size))
            emit(MatcherUseCase.MatcherState.CandidateLoaded)
            emit(
                MatcherUseCase.MatcherState.Success(
                    matchResultItems = responseItems,
                    totalCandidates = responseItems.size,
                    matcherName = MATCHER_NAME,
                    matchBatches = batches,
                ),
            )
        }

        coJustRun { saveMatchEvent.invoke(any(), any(), any(), any(), any(), any(), any()) }

        val states = viewModel.matchState.test()

        viewModel.setupMatch(
            MatchParams(
                probeReferenceId = "referenceId",
                probeFingerprintSamples = listOf(getFingerprintSample()),
                bioSdk = SECUGEN_SIM_MATCHER,
                flowType = FlowType.ENROL,
                queryForCandidates = mockk {},
                biometricDataSource = BiometricDataSource.Simprints,
            ),
        )
        // Waiting for the ::delay in viewModel::setupMatch
        advanceUntilIdle()

        assertThat(states.valueHistory()).isEqualTo(
            listOf(
                MatchViewModel.MatchState.NotStarted,
                MatchViewModel.MatchState.LoadingCandidates(responseItems.size, 0),
                MatchViewModel.MatchState.LoadingCandidates(responseItems.size, 1),
                MatchViewModel.MatchState.Finished(7, 7, 3, 2, 1),
            ),
        )
        assertThat(viewModel.matchResponse.getOrAwaitValue().peekContent()).isEqualTo(
            MatchResult(responseItems, SECUGEN_SIM_MATCHER),
        )

        verify {
            saveMatchEvent.invoke(
                any(),
                any(),
                any(),
                eq(7),
                eq(MATCHER_NAME),
                any(),
                withArg { list -> assertThat(list.size).isEqualTo(batches.size) },
            )
        }
    }

    @Test
    fun `Handle missing decision policy`() = runTest {
        coEvery {
            configManager
                .getProjectConfiguration()
                .face
                ?.getSdkConfiguration(any())
                ?.decisionPolicy
        } returns null

        val responseItems = listOf(
            MatchConfidence("1", 90f),
            MatchConfidence("1", 10f),
        )
        coEvery { faceMatcherUseCase.invoke(any(), any()) } returns flow {
            emit(MatcherUseCase.MatcherState.LoadingStarted(responseItems.size))
            emit(
                MatcherUseCase.MatcherState.Success(
                    matchResultItems = responseItems,
                    totalCandidates = responseItems.size,
                    matcherName = MATCHER_NAME,
                    matchBatches = emptyList(),
                ),
            )
        }

        val states = viewModel.matchState.test()
        viewModel.setupMatch(
            MatchParams(
                probeReferenceId = "referenceId",
                probeFaceSamples = listOf(getFaceSample()),
                bioSdk = FaceConfiguration.BioSdk.RANK_ONE,
                flowType = FlowType.ENROL,
                queryForCandidates = mockk {},
                biometricDataSource = BiometricDataSource.Simprints,
            ),
        )
        // Waiting for the ::delay in viewModel::setupMatch
        advanceUntilIdle()

        assertThat(states.valueHistory()).isEqualTo(
            listOf(
                MatchViewModel.MatchState.NotStarted,
                MatchViewModel.MatchState.LoadingCandidates(responseItems.size, 0),
                MatchViewModel.MatchState.Finished(2, 2, 1, 0, 0),
            ),
        )
    }

    private fun getFaceSample(): CaptureSample = CaptureSample(
        captureEventId = UUID.randomUUID().toString(),
        modality = Modality.FACE,
        template = Random.nextBytes(20),
        format = "format",
    )

    private fun getFingerprintSample(): CaptureSample = CaptureSample(
        captureEventId = UUID.randomUUID().toString(),
        modality = Modality.FINGERPRINT,
        template = Random.nextBytes(20),
        format = "format",
        identifier = SampleIdentifier.LEFT_3RD_FINGER,
    )

    companion object {
        const val MATCHER_NAME = "any matcher"
    }
}
