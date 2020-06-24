package com.simprints.face.match

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.face.FixtureGenerator
import com.simprints.face.controllers.core.crashreport.FaceCrashReportManager
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.flow.Action
import com.simprints.face.controllers.core.flow.MasterFlowManager
import com.simprints.face.controllers.core.preferencesManager.FacePreferencesManager
import com.simprints.face.controllers.core.repository.FaceDbManager
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.data.db.person.FaceSample
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.data.moduleapi.face.responses.FaceMatchResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.id.tools.utils.generateSequenceN
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.asFlow
import org.junit.Rule
import org.junit.Test
import java.io.Serializable

class FaceMatchViewModelTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcherProvider = object : DispatcherProvider {
        override fun main(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun default(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun io(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun unconfined(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val mockQuery: Serializable = mockk()
    private val verifyProbe = FaceSample("tsu-123-verify", "tsu-123-verify".toByteArray())
    private val identifyProbe = FaceSample("tsu-123-identify", "tsu-123-identify".toByteArray())
    private val verifyRequest = FaceMatchRequest(listOf(verifyProbe), mockQuery)
    private val identifyRequest = FaceMatchRequest(listOf(identifyProbe), mockQuery)

    private val masterFlowManager: MasterFlowManager = mockk()
    private val faceDbManager: FaceDbManager = mockk()
    private val facePreferencesManager: FacePreferencesManager = mockk() {
        every { faceMatchThreshold } returns 0f
    }
    private val faceMatcher: FaceMatcher = spyk()
    private val faceCrashReportManager: FaceCrashReportManager = mockk(relaxUnitFun = true)
    private val faceSessionEventsManager: FaceSessionEventsManager = mockk(relaxUnitFun = true)
    private val faceTimeHelper: FaceTimeHelper = mockk() {
        every { now() } returns 0
    }
    private val viewModel: FaceMatchViewModel =
        FaceMatchViewModel(
            masterFlowManager,
            faceDbManager,
            faceMatcher,
            facePreferencesManager,
            faceCrashReportManager,
            faceSessionEventsManager,
            faceTimeHelper,
            testDispatcherProvider
        )

    @Test
    fun `Route correctly to an error if it's not identify or verify`() {
        every { masterFlowManager.getCurrentAction() } returns Action.ENROL

        viewModel.setupMatch(identifyRequest)

        assertThat(viewModel.matchState.value).isEqualTo(FaceMatchViewModel.MatchState.Error)
    }

    @Test
    fun `Send events with correct values for identification`() = testCoroutineRule.runBlockingTest {
        every { masterFlowManager.getCurrentAction() } returns Action.IDENTIFY
        // Doing this way so I can compare later
        val candidates = generateSequenceN(5) { FixtureGenerator.getFaceIdentity(2) }.toList()
        coEvery { faceDbManager.loadPeople(any()) } returns candidates.asFlow()
        coEvery { faceMatcher.getComparisonScore(any(), any()) } returnsMany listOf(
            90f, // person 1
            80f,
            60f, // person 2
            60f,
            70f, // person 3
            70f,
            10f, // person 4
            20f,
            50f, // person 5
            55f
        )
        val matchStateObserver = viewModel.matchState.testObserver()

        viewModel.setupMatch(identifyRequest)

        assertThat(matchStateObserver.observedValues.size).isEqualTo(4)
        with(matchStateObserver.observedValues) {
            assertThat(get(0)).isEqualTo(FaceMatchViewModel.MatchState.NotStarted)
            assertThat(get(1)).isEqualTo(FaceMatchViewModel.MatchState.LoadingCandidates)
            assertThat(get(2)).isEqualTo(FaceMatchViewModel.MatchState.Matching)
            assertThat(get(3)).isEqualTo(FaceMatchViewModel.MatchState.Finished(5, 5, 4, 0, 1))
        }

        assertThat(viewModel.faceMatchResponse.value?.getContentIfNotHandled()).isEqualTo(
            FaceMatchResponse(
                listOf(
                    FaceMatchResult(candidates[0].faceId, 90f),
                    FaceMatchResult(candidates[2].faceId, 70f),
                    FaceMatchResult(candidates[1].faceId, 60f),
                    FaceMatchResult(candidates[4].faceId, 55f),
                    FaceMatchResult(candidates[3].faceId, 20f)
                )
            )
        )
    }

    @Test
    fun `Send events with correct values after filter for identification`() = testCoroutineRule.runBlockingTest {
        every { masterFlowManager.getCurrentAction() } returns Action.IDENTIFY
        // Doing this way so I can compare later
        val candidates = generateSequenceN(5) { FixtureGenerator.getFaceIdentity(2) }.toList()
        every { facePreferencesManager.faceMatchThreshold } returns 60f
        coEvery { faceDbManager.loadPeople(any()) } returns candidates.asFlow()
        coEvery { faceMatcher.getComparisonScore(any(), any()) } returnsMany listOf(
            90f, // person 1
            80f,
            60f, // person 2
            60f,
            70f, // person 3
            70f,
            10f, // person 4
            20f,
            50f, // person 5
            55f
        )
        val matchStateObserver = viewModel.matchState.testObserver()

        viewModel.setupMatch(identifyRequest)

        assertThat(matchStateObserver.observedValues.size).isEqualTo(4)
        with(matchStateObserver.observedValues) {
            assertThat(get(0)).isEqualTo(FaceMatchViewModel.MatchState.NotStarted)
            assertThat(get(1)).isEqualTo(FaceMatchViewModel.MatchState.LoadingCandidates)
            assertThat(get(2)).isEqualTo(FaceMatchViewModel.MatchState.Matching)
            assertThat(get(3)).isEqualTo(FaceMatchViewModel.MatchState.Finished(5, 3, 3, 0, 0))
        }

        assertThat(viewModel.faceMatchResponse.value?.getContentIfNotHandled()).isEqualTo(
            FaceMatchResponse(
                listOf(
                    FaceMatchResult(candidates[0].faceId, 90f),
                    FaceMatchResult(candidates[2].faceId, 70f),
                    FaceMatchResult(candidates[1].faceId, 60f)
                )
            )
        )
    }

    @Test
    fun `Send events with correct values for verification`() = testCoroutineRule.runBlockingTest {
        every { masterFlowManager.getCurrentAction() } returns Action.VERIFY
        // Doing this way so I can compare later
        val candidates = generateSequenceN(1) { FixtureGenerator.getFaceIdentity(2) }.toList()
        coEvery { faceDbManager.loadPeople(any()) } returns candidates.asFlow()
        coEvery { faceMatcher.getComparisonScore(any(), any()) } returnsMany listOf(90f, 80f)
        val matchStateObserver = viewModel.matchState.testObserver()

        viewModel.setupMatch(verifyRequest)

        assertThat(matchStateObserver.observedValues.size).isEqualTo(4)
        with(matchStateObserver.observedValues) {
            assertThat(get(0)).isEqualTo(FaceMatchViewModel.MatchState.NotStarted)
            assertThat(get(1)).isEqualTo(FaceMatchViewModel.MatchState.LoadingCandidates)
            assertThat(get(2)).isEqualTo(FaceMatchViewModel.MatchState.Matching)
            assertThat(get(3)).isEqualTo(FaceMatchViewModel.MatchState.Finished(1, 1, 1, 0, 0))
        }

        assertThat(viewModel.faceMatchResponse.value?.getContentIfNotHandled()).isEqualTo(
            FaceMatchResponse(listOf(FaceMatchResult(candidates[0].faceId, 90f)))
        )
    }

}
