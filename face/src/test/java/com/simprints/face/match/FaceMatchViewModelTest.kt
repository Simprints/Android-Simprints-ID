package com.simprints.face.match

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.face.FixtureGenerator
import com.simprints.face.controllers.core.crashreport.FaceCrashReportManager
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.*
import com.simprints.face.controllers.core.events.model.Matcher
import com.simprints.face.controllers.core.flow.Action
import com.simprints.face.controllers.core.flow.MasterFlowManager
import com.simprints.face.controllers.core.repository.FaceDbManager
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.data.db.person.FaceSample
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.data.moduleapi.face.responses.FaceMatchResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.id.tools.utils.generateSequenceN
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.*
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
    private val faceMatcher: FaceMatcher = spyk()
    private val faceCrashReportManager: FaceCrashReportManager = mockk(relaxUnitFun = true)
    private val faceSessionEventsManager: FaceSessionEventsManager = mockk(relaxUnitFun = true)
    private val faceTimeHelper: FaceTimeHelper = mockk {
        every { now() } returns 0
    }
    private lateinit var viewModel: FaceMatchViewModel

    @Test
    fun `Handle enrolment+ (identification during enrolment) correctly`() {
        viewModel = newFaceMatchViewModel()
        every { masterFlowManager.getCurrentAction() } returns Action.ENROL

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
        val eventCapture: CapturingSlot<Event> = slot()
        every { faceTimeHelper.now() } returnsMany (0..100L).toList()
        every { faceSessionEventsManager.addEventInBackground(capture(eventCapture)) } just Runs

        viewModel.setupMatch(identifyRequest)

        assertThat(matchStateObserver.observedValues.size).isEqualTo(4)
        with(matchStateObserver.observedValues) {
            assertThat(get(0)).isEqualTo(FaceMatchViewModel.MatchState.NotStarted)
            assertThat(get(1)).isEqualTo(FaceMatchViewModel.MatchState.LoadingCandidates)
            assertThat(get(2)).isEqualTo(FaceMatchViewModel.MatchState.Matching)
            assertThat(get(3)).isEqualTo(FaceMatchViewModel.MatchState.Finished(5, 5, 4, 0, 1))
        }

        val responseResults = listOf(
            FaceMatchResult(candidates[0].faceId, 90f),
            FaceMatchResult(candidates[2].faceId, 70f),
            FaceMatchResult(candidates[1].faceId, 60f),
            FaceMatchResult(candidates[4].faceId, 55f),
            FaceMatchResult(candidates[3].faceId, 20f)
        )

        assertThat(viewModel.faceMatchResponse.value?.getContentIfNotHandled()).isEqualTo(
            FaceMatchResponse(responseResults)
        )

        val eventEntries = responseResults.map { MatchEntry(it.guid, it.confidence) }
        with(eventCapture.captured as OneToManyMatchEvent) {
            assertThat(startTime).isEqualTo(0)
            assertThat(endTime).isEqualTo(1)
            assertThat(count).isEqualTo(5)
            assertThat(matcher).isEqualTo(Matcher.UNKNOWN)
            assertThat(query).isEqualTo(mockQuery)
            assertThat(result).isEqualTo(eventEntries)
        }

        verify(atMost = 1) { faceSessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun `Send events with correct values for identification`() = testCoroutineRule.runBlockingTest {
        viewModel = newFaceMatchViewModel()
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
        val eventCapture: CapturingSlot<Event> = slot()
        every { faceTimeHelper.now() } returnsMany (0..100L).toList()
        every { faceSessionEventsManager.addEventInBackground(capture(eventCapture)) } just Runs

        viewModel.setupMatch(identifyRequest)

        assertThat(matchStateObserver.observedValues.size).isEqualTo(4)
        with(matchStateObserver.observedValues) {
            assertThat(get(0)).isEqualTo(FaceMatchViewModel.MatchState.NotStarted)
            assertThat(get(1)).isEqualTo(FaceMatchViewModel.MatchState.LoadingCandidates)
            assertThat(get(2)).isEqualTo(FaceMatchViewModel.MatchState.Matching)
            assertThat(get(3)).isEqualTo(FaceMatchViewModel.MatchState.Finished(5, 5, 4, 0, 1))
        }

        val responseResults = listOf(
            FaceMatchResult(candidates[0].faceId, 90f),
            FaceMatchResult(candidates[2].faceId, 70f),
            FaceMatchResult(candidates[1].faceId, 60f),
            FaceMatchResult(candidates[4].faceId, 55f),
            FaceMatchResult(candidates[3].faceId, 20f)
        )

        assertThat(viewModel.faceMatchResponse.value?.getContentIfNotHandled()).isEqualTo(
            FaceMatchResponse(responseResults)
        )

        val eventEntries = responseResults.map { MatchEntry(it.guid, it.confidence) }
        with(eventCapture.captured as OneToManyMatchEvent) {
            assertThat(startTime).isEqualTo(0)
            assertThat(endTime).isEqualTo(1)
            assertThat(count).isEqualTo(5)
            assertThat(matcher).isEqualTo(Matcher.UNKNOWN)
            assertThat(query).isEqualTo(mockQuery)
            assertThat(result).isEqualTo(eventEntries)
        }

        verify(atMost = 1) { faceSessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun `Send events with correct values for verification`() = testCoroutineRule.runBlockingTest {
        viewModel = newFaceMatchViewModel()
        every { masterFlowManager.getCurrentAction() } returns Action.VERIFY
        // Doing this way so I can compare later
        val candidates = generateSequenceN(1) { FixtureGenerator.getFaceIdentity(2) }.toList()
        coEvery { faceDbManager.loadPeople(any()) } returns candidates.asFlow()
        coEvery { faceMatcher.getComparisonScore(any(), any()) } returnsMany listOf(90f, 80f)
        val matchStateObserver = viewModel.matchState.testObserver()
        val eventCapture: CapturingSlot<Event> = slot()
        every { faceTimeHelper.now() } returnsMany (0..100L).toList()
        every { faceSessionEventsManager.addEventInBackground(capture(eventCapture)) } just Runs

        viewModel.setupMatch(verifyRequest)

        assertThat(matchStateObserver.observedValues.size).isEqualTo(4)
        with(matchStateObserver.observedValues) {
            assertThat(get(0)).isEqualTo(FaceMatchViewModel.MatchState.NotStarted)
            assertThat(get(1)).isEqualTo(FaceMatchViewModel.MatchState.LoadingCandidates)
            assertThat(get(2)).isEqualTo(FaceMatchViewModel.MatchState.Matching)
            assertThat(get(3)).isEqualTo(FaceMatchViewModel.MatchState.Finished(1, 1, 1, 0, 0))
        }

        val responseResult = FaceMatchResult(candidates[0].faceId, 90f)

        assertThat(viewModel.faceMatchResponse.value?.getContentIfNotHandled()).isEqualTo(
            FaceMatchResponse(listOf(responseResult))
        )

        val eventEntry = MatchEntry(responseResult.guid, responseResult.confidence)
        with(eventCapture.captured as OneToOneMatchEvent) {
            assertThat(startTime).isEqualTo(0)
            assertThat(endTime).isEqualTo(1)
            assertThat(matcher).isEqualTo(Matcher.UNKNOWN)
            assertThat(query).isEqualTo(mockQuery)
            assertThat(result).isEqualTo(eventEntry)
        }

        verify(atMost = 1) { faceSessionEventsManager.addEventInBackground(any()) }
    }

    private fun newFaceMatchViewModel(): FaceMatchViewModel {
        return FaceMatchViewModel(
            masterFlowManager,
            faceDbManager,
            faceMatcher,
            faceCrashReportManager,
            faceSessionEventsManager,
            faceTimeHelper,
            testDispatcherProvider
        )
    }
}
