package com.simprints.face.orchestrator

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.face.data.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.face.data.moduleapi.face.responses.entities.FaceSample
import com.simprints.face.data.moduleapi.face.responses.entities.Path
import com.simprints.face.data.moduleapi.face.responses.entities.SecuredImageRef
import com.simprints.infra.events.sampledata.FACE_TEMPLATE_FORMAT
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import com.simprints.moduleapi.face.responses.IFaceCaptureResponse
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class FaceOrchestratorViewModelTest {
    private val viewModel = spyk(FaceOrchestratorViewModel())

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `correctly start the viewmodel - single capture`() {
        val startCaptureObserver = viewModel.startCapture.testObserver()

        viewModel.start(generateCaptureRequest(1))

        assertThat(startCaptureObserver.observedValues.size).isEqualTo(1)
    }

    @Test
    fun `return the correct capture response on finish`() {
        viewModel.captureFinished(generateFakeCaptureResponse())

        viewModel.flowFinished.value?.let { liveData ->
            (liveData.peekContent() as IFaceCaptureResponse).capturingResult[0].let {
                assertThat(it.sample?.template?.size).isEqualTo(0)
                assertThat(it.sample?.faceId).isNotEmpty()
                assertThat(it.sample?.imageRef?.path?.parts).isEqualTo(arrayOf("file://someFile"))
                assertThat(it.sample?.format).isEqualTo(FACE_TEMPLATE_FORMAT)
            }
        }
    }

    private fun generateCaptureRequest(captures: Int) = mockk<IFaceCaptureRequest> {
        every { nFaceSamplesToCapture } returns captures
    }

    private fun generateFakeCaptureResponse(): FaceCaptureResponse {
        val securedImageRef = SecuredImageRef(
            path = Path(arrayOf("file://someFile"))
        )
        val sample =
            FaceSample(UUID.randomUUID().toString(), ByteArray(0), securedImageRef, FACE_TEMPLATE_FORMAT)
        val result = FaceCaptureResult(0, sample)
        val captureResults = listOf(result)
        return FaceCaptureResponse(captureResults)
    }

}
