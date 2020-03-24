package com.simprints.face.orchestrator

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.face.activities.FaceCaptureActivity
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import com.simprints.moduleapi.face.responses.IFaceCaptureResponse
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
        assertThat(viewModel.faceRequest).isInstanceOf(FaceCaptureRequest::class.java)

        assertThat(startCaptureObserver.observedValues.size).isEqualTo(1)
    }

    @Test
    fun `return the correct response on finish`() {
        viewModel.captureFinished(FaceCaptureActivity.generateFakeCaptureResponse())

        viewModel.flowFinished.value?.let { liveData ->
            (liveData.peekContent() as IFaceCaptureResponse).capturingResult[0].let {
                assertThat(it.sample?.template?.size).isEqualTo(0)
                assertThat(it.sample?.faceId).isNotEmpty()
                assertThat(it.sample?.imageRef?.path?.parts).isEqualTo(arrayOf("file://someFile"))
            }

        }

    }

    private fun generateCaptureRequest(captures: Int) = mockk<IFaceCaptureRequest> {
        every { nFaceSamplesToCapture } returns captures
    }
}
