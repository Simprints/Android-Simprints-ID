package com.simprints.face.matcher.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.FlowProvider
import com.simprints.core.tools.time.TimeHelper
import com.simprints.face.matcher.FaceMatchParams
import com.simprints.face.matcher.FaceMatchResult
import com.simprints.face.matcher.usecases.LoadPeopleFaceIdentityUseCase
import com.simprints.face.matcher.usecases.SaveMatchEventUseCase
import com.simprints.infra.facebiosdk.matching.FaceIdentity
import com.simprints.infra.facebiosdk.matching.FaceMatcher
import com.simprints.infra.facebiosdk.matching.FaceSample
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID
import kotlin.random.Random

internal class FaceMatchViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()


    @MockK
    lateinit var loadPeopleFaceIdentities: LoadPeopleFaceIdentityUseCase

    @MockK
    lateinit var faceMatcher: FaceMatcher

    @MockK
    lateinit var saveMatchEvent: SaveMatchEventUseCase

    @MockK
    lateinit var timeHelper: TimeHelper

    lateinit var viewModel: FaceMatchViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns 0
        every { faceMatcher.matcherName } returns MATCHER_NAME

        viewModel = FaceMatchViewModel(loadPeopleFaceIdentities, faceMatcher, saveMatchEvent, timeHelper)
    }

    @Test
    fun `Handle match request correctly`() = runTest {
        val candidates = List(7) { getFaceIdentity(1) }
        val comparisonScores = listOf(90f, 80f, 55f, 40f, 36f, 20f, 10f)
        val responseItems = candidates.mapIndexed { index, faceIdentity ->
            FaceMatchResult.Item(faceIdentity.faceId, comparisonScores[index])
        }

        coEvery { loadPeopleFaceIdentities.invoke(any()) } returns candidates.asFlow()
        coEvery { faceMatcher.getHighestComparisonScoreForCandidate(any(), any()) } returnsMany comparisonScores
        coEvery { saveMatchEvent.invoke(any(), any(), any(), any(), any(), any()) } just Runs


        viewModel.setupMatch(FaceMatchParams(
            probeFaceSamples = emptyList(),
            flowType = FlowProvider.FlowType.ENROL,
            queryForCandidates = mockk {}
        ))

        assertThat(viewModel.matchState.getOrAwaitValue()).isEqualTo(
            FaceMatchViewModel.MatchState.Finished(7, 7, 3, 2, 1)
        )
        assertThat(viewModel.faceMatchResponse.getOrAwaitValue().peekContent()).isEqualTo(
            FaceMatchResult(responseItems)
        )

        verify { saveMatchEvent.invoke(any(), any(), any(), eq(7), eq(MATCHER_NAME), any()) }
    }


    private fun getFaceIdentity(numFaces: Int): FaceIdentity = FaceIdentity(
        UUID.randomUUID().toString(),
        List(numFaces) { getFaceSample() }
    )

    private fun getFaceSample(): FaceSample =
        FaceSample(UUID.randomUUID().toString(), Random.nextBytes(20))


    companion object {
        const val MATCHER_NAME = "any matcher"
    }
}
