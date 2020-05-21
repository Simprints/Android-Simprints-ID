package com.simprints.face.match

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.face.controllers.core.flow.Action
import com.simprints.face.controllers.core.flow.MasterFlowManager
import com.simprints.face.controllers.core.repository.FaceDbManager
import com.simprints.face.data.db.person.FaceSample
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.Serializable

class FaceMatchViewModelTest {
    private val mockQuery: Serializable = mockk()
    private val verifyProbe = FaceSample("tsu-123-verify", "tsu-123-verify".toByteArray())
    private val identifyProbe = FaceSample("tsu-123-identify", "tsu-123-identify".toByteArray())
    private val verifyRequest = FaceMatchRequest(listOf(verifyProbe), mockQuery)
    private val identifyRequest = FaceMatchRequest(listOf(identifyProbe), mockQuery)

    private val faceDbManager: FaceDbManager = mockk()
    private val masterFlowManager: MasterFlowManager = mockk()
    private val viewModel: FaceMatchViewModel = FaceMatchViewModel(masterFlowManager, faceDbManager)

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `Route correctly to verification`() {
        every { masterFlowManager.getCurrentAction() } returns Action.VERIFY

        viewModel.setupMatch(verifyRequest)

        assertThat(viewModel.matchState.value).isEqualTo(FaceMatchViewModel.MatchState.NOT_STARTED_VERIFY)
    }

    @Test
    fun `Route correctly to identification`() {
        every { masterFlowManager.getCurrentAction() } returns Action.IDENTIFY

        viewModel.setupMatch(identifyRequest)

        assertThat(viewModel.matchState.value).isEqualTo(FaceMatchViewModel.MatchState.NOT_STARTED_IDENTIFY)
    }

    @Test
    fun `Route correctly to an error`() {
        every { masterFlowManager.getCurrentAction() } returns Action.ENROL

        viewModel.setupMatch(identifyRequest)

        assertThat(viewModel.matchState.value).isEqualTo(FaceMatchViewModel.MatchState.ERROR)
    }
}
