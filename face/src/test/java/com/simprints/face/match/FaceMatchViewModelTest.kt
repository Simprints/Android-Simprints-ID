package com.simprints.face.match

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.face.PeopleGenerator
import com.simprints.face.controllers.core.flow.Action
import com.simprints.face.controllers.core.flow.MasterFlowManager
import com.simprints.face.controllers.core.repository.FaceDbManager
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
    private val faceMatcher: FaceMatcher = spyk()
    private val viewModel: FaceMatchViewModel =
        FaceMatchViewModel(masterFlowManager, faceDbManager, faceMatcher, testDispatcherProvider)

    @Test
    fun `Route correctly to an error if it's not identify or verify`() {
        every { masterFlowManager.getCurrentAction() } returns Action.ENROL

        viewModel.setupMatch(identifyRequest)

        assertThat(viewModel.matchState.value).isEqualTo(FaceMatchViewModel.MatchState.ERROR)
    }

    @Test
    fun `Send events with correct values for identification`() = testCoroutineRule.runBlockingTest {
        every { masterFlowManager.getCurrentAction() } returns Action.IDENTIFY
        // Doing this way so I can compare later
        val candidates = generateSequenceN(5) { PeopleGenerator.getFaceIdentity(2) }.toList()
        coEvery { faceDbManager.loadPeople(any()) } returns candidates.asFlow()
        coEvery { faceMatcher.getComparisonScore(any(), any()) } returnsMany listOf(
            0.9f, // person 1
            0.8f,
            0.6f, // person 2
            0.6f,
            0.7f, // person 3
            0.7f,
            0.1f, // person 4
            0.2f,
            0.5f, // person 5
            0.55f
        )
        val matchStateObserver = viewModel.matchState.testObserver()

        viewModel.setupMatch(identifyRequest)

        assertThat(matchStateObserver.observedValues.size).isEqualTo(4)
        with(matchStateObserver.observedValues) {
            assertThat(get(0)).isEqualTo(FaceMatchViewModel.MatchState.NOT_STARTED_IDENTIFY)
            assertThat(get(1)).isEqualTo(FaceMatchViewModel.MatchState.LOADING_CANDIDATES)
            assertThat(get(2)).isEqualTo(FaceMatchViewModel.MatchState.MATCHING)
            assertThat(get(3)).isEqualTo(FaceMatchViewModel.MatchState.FINISHED)
        }

        assertThat(viewModel.faceMatchResponse.value?.getContentIfNotHandled()).isEqualTo(
            FaceMatchResponse(
                listOf(
                    FaceMatchResult(candidates[0].faceId, 0.9f),
                    FaceMatchResult(candidates[2].faceId, 0.7f),
                    FaceMatchResult(candidates[1].faceId, 0.6f),
                    FaceMatchResult(candidates[4].faceId, 0.55f),
                    FaceMatchResult(candidates[3].faceId, 0.2f)
                )
            )
        )
    }

    @Test
    fun `Send events with correct values for verification`() = testCoroutineRule.runBlockingTest {
        every { masterFlowManager.getCurrentAction() } returns Action.VERIFY
        // Doing this way so I can compare later
        val candidates = generateSequenceN(1) { PeopleGenerator.getFaceIdentity(2) }.toList()
        coEvery { faceDbManager.loadPeople(any()) } returns candidates.asFlow()
        coEvery { faceMatcher.getComparisonScore(any(), any()) } returnsMany listOf(0.9f, 0.8f)
        val matchStateObserver = viewModel.matchState.testObserver()

        viewModel.setupMatch(verifyRequest)

        assertThat(matchStateObserver.observedValues.size).isEqualTo(4)
        with(matchStateObserver.observedValues) {
            assertThat(get(0)).isEqualTo(FaceMatchViewModel.MatchState.NOT_STARTED_VERIFY)
            assertThat(get(1)).isEqualTo(FaceMatchViewModel.MatchState.LOADING_CANDIDATES)
            assertThat(get(2)).isEqualTo(FaceMatchViewModel.MatchState.MATCHING)
            assertThat(get(3)).isEqualTo(FaceMatchViewModel.MatchState.FINISHED)
        }

        assertThat(viewModel.faceMatchResponse.value?.getContentIfNotHandled()).isEqualTo(
            FaceMatchResponse(listOf(FaceMatchResult(candidates[0].faceId, 0.9f)))
        )
    }

}
