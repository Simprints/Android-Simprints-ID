package com.simprints.face.activities.orchestrator

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Rule
import org.junit.Test

class FaceOrchestratorViewModelTest {
    private val viewModel = spyk(FaceOrchestratorViewModel())

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `correctly start the viewmodel - single capture`() {
        val startCaptureObserver = viewModel.startCapture.testObserver()

        viewModel.start(generateCaptureRequest(1))
        Truth.assertThat(viewModel.faceRequest).isInstanceOf(FaceCaptureRequest::class.java)

        Truth.assertThat(startCaptureObserver.observedValues.size).isEqualTo(1)
    }

    private fun generateCaptureRequest(captures: Int) = mockk<IFaceCaptureRequest> {
        every { nFaceSamplesToCapture } returns captures
    }
}
