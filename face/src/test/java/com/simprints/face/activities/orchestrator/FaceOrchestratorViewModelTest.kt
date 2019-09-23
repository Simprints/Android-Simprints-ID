package com.simprints.face.activities.orchestrator

import com.google.common.truth.Truth
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test

class FaceOrchestratorViewModelTest {
    private val viewModel = spyk(FaceOrchestratorViewModel())

    @Test
    fun `correctly start the viewmodel`() {
        viewModel.start(singleCaptureRequest)
        Truth.assertThat(viewModel.faceRequest).isInstanceOf(FaceCaptureRequest::class.java)
    }

    private val singleCaptureRequest = mockk<IFaceCaptureRequest> {
        every { nFaceSamplesToCapture } returns 1
    }
}
