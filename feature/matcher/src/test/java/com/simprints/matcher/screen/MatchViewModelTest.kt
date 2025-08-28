package com.simprints.matcher.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.matcher.MatchParams
import com.simprints.matcher.MatchResult
import com.simprints.matcher.MatchResultItem
import com.simprints.matcher.usecases.FaceMatcherUseCase
import com.simprints.matcher.usecases.FingerprintMatcherUseCase
import com.simprints.matcher.usecases.MatcherUseCase
import com.simprints.matcher.usecases.SaveMatchEventUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

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
    private val totalCandidates = 384
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
            MatchResultItem("1", 90f),
        )

        coEvery { faceMatcherUseCase.invoke(any(), any()) } returns flow {
            emit(MatcherUseCase.MatcherState.LoadingStarted(responseItems.size))
            emit(MatcherUseCase.MatcherState.CandidateLoaded)
            emit(
                MatcherUseCase.MatcherState.Success(
                    matchResultItems = responseItems,
                    totalCandidates = responseItems.size,
                    matcherName = "MatcherName",
                ),
            )
        }
        coJustRun { saveMatchEvent.invoke(any(), any(), any(), any(), any(), any()) }

        assertThat(viewModel.isInitialized).isFalse()

        viewModel.matchState.test()
        viewModel.setupMatch(
            MatchParams(
                probeReferenceId = "referenceId",
                probeSamples = listOf(getFaceSample()),
                sdkType = FaceConfiguration.BioSdk.RANK_ONE,
                modality = Modality.FACE,
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
                ?.rankOne
                ?.decisionPolicy
        } returns DecisionPolicy(20, 35, 50)

        val responseItems = listOf(
            MatchResultItem("1", 90f),
            MatchResultItem("1", 80f),
            MatchResultItem("1", 55f),
            MatchResultItem("1", 40f),
            MatchResultItem("1", 36f),
            MatchResultItem("1", 20f),
            MatchResultItem("1", 10f),
        )
        coEvery { faceMatcherUseCase.invoke(any(), any()) } returns flow {
            emit(MatcherUseCase.MatcherState.LoadingStarted(responseItems.size))
            emit(MatcherUseCase.MatcherState.CandidateLoaded)
            emit(
                MatcherUseCase.MatcherState.Success(
                    matchResultItems = responseItems,
                    totalCandidates = responseItems.size,
                    matcherName = MATCHER_NAME,
                ),
            )
        }
        coJustRun { saveMatchEvent.invoke(any(), any(), any(), any(), any(), any()) }

        val states = viewModel.matchState.test()
        viewModel.setupMatch(
            MatchParams(
                probeReferenceId = "referenceId",
                probeSamples = listOf(getFaceSample()),
                modality = Modality.FACE,
                sdkType = FaceConfiguration.BioSdk.RANK_ONE,
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
            MatchResult(responseItems, Modality.FACE, FaceConfiguration.BioSdk.RANK_ONE),
        )

        verify { saveMatchEvent.invoke(any(), any(), any(), eq(7), eq(MATCHER_NAME), any()) }
    }

    @Test
    fun `Handle fingerprint match request correctly`() = runTest {
        coEvery {
            configManager
                .getProjectConfiguration()
                .fingerprint
                ?.secugenSimMatcher
                ?.decisionPolicy
        } returns DecisionPolicy(200, 350, 500)

        val responseItems = listOf(
            MatchResultItem("1", 900f),
            MatchResultItem("1", 800f),
            MatchResultItem("1", 550f),
            MatchResultItem("1", 400f),
            MatchResultItem("1", 360f),
            MatchResultItem("1", 200f),
            MatchResultItem("1", 100f),
        )

        coEvery { fingerprintMatcherUseCase.invoke(any(), any()) } returns flow {
            emit(MatcherUseCase.MatcherState.LoadingStarted(responseItems.size))
            emit(MatcherUseCase.MatcherState.CandidateLoaded)
            emit(
                MatcherUseCase.MatcherState.Success(
                    matchResultItems = responseItems,
                    totalCandidates = responseItems.size,
                    matcherName = MATCHER_NAME,
                ),
            )
        }

        coJustRun { saveMatchEvent.invoke(any(), any(), any(), any(), any(), any()) }

        val states = viewModel.matchState.test()

        viewModel.setupMatch(
            MatchParams(
                probeReferenceId = "referenceId",
                probeSamples = listOf(getFingerprintSample()),
                modality = Modality.FINGERPRINT,
                sdkType = SECUGEN_SIM_MATCHER,
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
            MatchResult(responseItems, Modality.FINGERPRINT, SECUGEN_SIM_MATCHER),
        )

        verify { saveMatchEvent.invoke(any(), any(), any(), eq(7), eq(MATCHER_NAME), any()) }
    }

    @Test
    fun `Handle missing decision policy`() = runTest {
        coEvery {
            configManager
                .getProjectConfiguration()
                .face
                ?.rankOne
                ?.decisionPolicy
        } returns null

        val responseItems = listOf(
            MatchResultItem("1", 90f),
            MatchResultItem("1", 10f),
        )
        coEvery { faceMatcherUseCase.invoke(any(), any()) } returns flow {
            emit(MatcherUseCase.MatcherState.LoadingStarted(responseItems.size))
            emit(
                MatcherUseCase.MatcherState.Success(
                    matchResultItems = responseItems,
                    totalCandidates = responseItems.size,
                    matcherName = MATCHER_NAME,
                ),
            )
        }

        val states = viewModel.matchState.test()
        viewModel.setupMatch(
            MatchParams(
                probeReferenceId = "referenceId",
                probeSamples = listOf(getFaceSample()),
                modality = Modality.FACE,
                sdkType = FaceConfiguration.BioSdk.RANK_ONE,
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

    private fun getFaceSample() = CaptureSample(
        format = "format",
        template = Random.nextBytes(20),
        templateQualityScore = 1,
        modality = Modality.FACE,
        imageRef = null,
    )

    private fun getFingerprintSample() = CaptureSample(
        identifier = SampleIdentifier.LEFT_3RD_FINGER,
        format = "format",
        template = Random.nextBytes(20),
        templateQualityScore = 1,
        modality = Modality.FINGERPRINT,
        imageRef = null,
    )

    companion object {
        const val MATCHER_NAME = "any matcher"
    }
}
