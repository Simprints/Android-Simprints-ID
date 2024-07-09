package com.simprints.matcher.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
import com.simprints.infra.enrolment.records.store.domain.models.BiometricDataSource
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.FingerprintMatchResult
import com.simprints.matcher.MatchParams
import com.simprints.matcher.usecases.FaceMatcherUseCase
import com.simprints.matcher.usecases.FingerprintMatcherUseCase
import com.simprints.matcher.usecases.MatcherUseCase
import com.simprints.matcher.usecases.SaveMatchEventUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID
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

    private lateinit var cb1: CapturingSlot<(String) -> Unit>

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
            timeHelper
        )
    }

    @Test
    fun `when setup is called, then view model becomes initialized`() = runTest {
        val responseItems = listOf(
            FaceMatchResult.Item("1", 90f),
        )

        coEvery { faceMatcherUseCase.invoke(any(), capture(cb1)) } answers {
            cb1.captured.invoke("tag1")
            MatcherUseCase.MatcherResult(
                matchResultItems = responseItems,
                totalCandidates = responseItems.size,
                matcherName = "MatcherName"
            )
        }
        coJustRun { saveMatchEvent.invoke(any(), any(), any(), any(), any(), any()) }

        assertThat(viewModel.isInitialized).isFalse()

        viewModel.matchState.test()
        viewModel.setupMatch(MatchParams(
            probeFaceSamples = listOf(getFaceSample()),
            flowType = FlowType.ENROL,
            queryForCandidates = mockk {},
            biometricDataSource = BiometricDataSource.Simprints,
        ))

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
        val responseItems = listOf(
            FaceMatchResult.Item("1", 90f),
            FaceMatchResult.Item("1", 80f),
            FaceMatchResult.Item("1", 55f),
            FaceMatchResult.Item("1", 40f),
            FaceMatchResult.Item("1", 36f),
            FaceMatchResult.Item("1", 20f),
            FaceMatchResult.Item("1", 10f),
        )

        coEvery { faceMatcherUseCase.invoke(any(), capture(cb1)) } answers {
            cb1.captured.invoke("tag1")
            MatcherUseCase.MatcherResult(
                matchResultItems = responseItems,
                totalCandidates = responseItems.size,
                matcherName = MATCHER_NAME
            )
        }
        coJustRun { saveMatchEvent.invoke(any(), any(), any(), any(), any(), any()) }

        val states = viewModel.matchState.test()
        viewModel.setupMatch(
            MatchParams(
                probeFaceSamples = listOf(getFaceSample()),
                flowType = FlowType.ENROL,
                queryForCandidates = mockk {},
                biometricDataSource = BiometricDataSource.Simprints,
            )
        )
        // Waiting for the ::delay in viewModel::setupMatch
        advanceUntilIdle()

        assertThat(states.valueHistory()).isEqualTo(
            listOf(
                MatchViewModel.MatchState.NotStarted,
                MatchViewModel.MatchState.LoadingCandidates,
                MatchViewModel.MatchState.Finished(7, 7, 3, 2, 1)
            )
        )
        assertThat(viewModel.matchResponse.getOrAwaitValue().peekContent()).isEqualTo(
            FaceMatchResult(responseItems)
        )

        verify { saveMatchEvent.invoke(any(), any(), any(), eq(7), eq(MATCHER_NAME), any()) }
    }

    @Test
    fun `Handle fingerprint match request correctly`() = runTest {
        val responseItems = listOf(
            FingerprintMatchResult.Item("1", 90f),
            FingerprintMatchResult.Item("1", 80f),
            FingerprintMatchResult.Item("1", 55f),
            FingerprintMatchResult.Item("1", 40f),
            FingerprintMatchResult.Item("1", 36f),
            FingerprintMatchResult.Item("1", 20f),
            FingerprintMatchResult.Item("1", 10f),
        )

        coEvery { fingerprintMatcherUseCase.invoke(any(), capture(cb1)) } answers {
            cb1.captured.invoke("tag1")
            MatcherUseCase.MatcherResult(
                matchResultItems = responseItems,
                totalCandidates = responseItems.size,
                matcherName = MATCHER_NAME
            )
        }
        coJustRun { saveMatchEvent.invoke(any(), any(), any(), any(), any(), any()) }

        val states = viewModel.matchState.test()

        viewModel.setupMatch(
            MatchParams(
                probeFingerprintSamples = listOf(getFingerprintSample()),
                fingerprintSDK = SECUGEN_SIM_MATCHER,
                flowType = FlowType.ENROL,
                queryForCandidates = mockk {},
                biometricDataSource = BiometricDataSource.Simprints,
            )
        )
        // Waiting for the ::delay in viewModel::setupMatch
        advanceUntilIdle()

        assertThat(states.valueHistory()).isEqualTo(
            listOf(
                MatchViewModel.MatchState.NotStarted,
                MatchViewModel.MatchState.LoadingCandidates,
                MatchViewModel.MatchState.Finished(7, 7, 3, 2, 1)
            )
        )
        assertThat(viewModel.matchResponse.getOrAwaitValue().peekContent()).isEqualTo(
            FingerprintMatchResult(responseItems, SECUGEN_SIM_MATCHER)
        )

        verify { saveMatchEvent.invoke(any(), any(), any(), eq(7), eq(MATCHER_NAME), any()) }
    }

    private fun getFaceSample(): MatchParams.FaceSample =
        MatchParams.FaceSample(UUID.randomUUID().toString(), Random.nextBytes(20))

    private fun getFingerprintSample(): MatchParams.FingerprintSample =
        MatchParams.FingerprintSample(
            IFingerIdentifier.LEFT_3RD_FINGER,
            "format",
            Random.nextBytes(20)
        )

    companion object {
        const val MATCHER_NAME = "any matcher"
    }
}
