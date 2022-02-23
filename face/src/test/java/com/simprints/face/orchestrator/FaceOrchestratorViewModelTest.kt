package com.simprints.face.orchestrator

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import com.simprints.face.FixtureGenerator.generateFaceMatchResults
import com.simprints.face.data.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.face.data.moduleapi.face.responses.FaceMatchResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.face.data.moduleapi.face.responses.entities.FaceSample
import com.simprints.face.data.moduleapi.face.responses.entities.Path
import com.simprints.face.data.moduleapi.face.responses.entities.SecuredImageRef
import com.simprints.face.error.ErrorType
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import com.simprints.moduleapi.face.responses.IFaceCaptureResponse
import com.simprints.moduleapi.face.responses.IFaceConfigurationResponse
import com.simprints.moduleapi.face.responses.IFaceErrorReason
import com.simprints.moduleapi.face.responses.IFaceErrorResponse
import com.simprints.moduleapi.face.responses.IFaceMatchResponse
import com.simprints.moduleapi.face.responses.entities.IFaceTemplateFormat
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
                assertThat(it.sample?.format).isEqualTo(IFaceTemplateFormat.RANK_ONE_1_23)
            }
        }
    }

    @Test
    fun `return the correct matching response on finish`() {
        viewModel.matchFinished(generateFakeMatchResponse())

        viewModel.flowFinished.value?.let { liveData ->
            (liveData.peekContent() as IFaceMatchResponse).result.let {
                assertThat(it[0].guid).isNotEmpty()
                assertThat(it[0].confidence).isIn(Range.closed(0f, 100f))
            }
        }
    }

    @Test
    fun `return the correct configuration response on finish`() {
        viewModel.configurationFinished(true)

        viewModel.flowFinished.value?.let { liveData ->
            assertThat(liveData.peekContent()).isInstanceOf(IFaceConfigurationResponse::class.java)
        }
    }

    @Test
    fun `route user to invalid license flow if needed`() {
        viewModel.invalidLicense()
        assertThat(viewModel.errorEvent.value?.peekContent()).isEqualTo(ErrorType.LICENSE_INVALID)
        viewModel.finishWithError(ErrorType.LICENSE_INVALID)
        viewModel.flowFinished.value?.peekContent()?.let { response ->
            assertThat(response).isInstanceOf(IFaceErrorResponse::class.java)
            assertThat((response as IFaceErrorResponse).reason).isEqualTo(IFaceErrorReason.LICENSE_INVALID)
        }
    }

    @Test
    fun `route user to missing license flow if needed`() {
        viewModel.missingLicense()
        assertThat(viewModel.errorEvent.value?.peekContent()).isEqualTo(ErrorType.LICENSE_MISSING)
        viewModel.finishWithError(ErrorType.LICENSE_MISSING)
        viewModel.flowFinished.value?.peekContent()?.let { response ->
            assertThat(response).isInstanceOf(IFaceErrorResponse::class.java)
            assertThat((response as IFaceErrorResponse).reason).isEqualTo(IFaceErrorReason.LICENSE_MISSING)
        }
    }

    @Test
    fun `route user to configuration error flow if needed`() {
        viewModel.configurationFinished(false)
        viewModel.finishWithError(ErrorType.CONFIGURATION_ERROR)
        viewModel.flowFinished.value?.peekContent()?.let { response ->
            assertThat(response).isInstanceOf(IFaceErrorResponse::class.java)
            assertThat((response as IFaceErrorResponse).reason).isEqualTo(IFaceErrorReason.FACE_CONFIGURATION_ERROR)
        }
    }

    @Test
    fun `route user to configuration error flow if needed when code is not null`() {
        viewModel.configurationFinished(false, errorCode = "98")
        viewModel.finishWithError(ErrorType.CONFIGURATION_ERROR)
        viewModel.flowFinished.value?.peekContent()?.let { response ->
            assertThat(response).isInstanceOf(IFaceErrorResponse::class.java)
            assertThat((response as IFaceErrorResponse).reason).isEqualTo(IFaceErrorReason.FACE_CONFIGURATION_ERROR)
        }
    }

    @Test
    fun `route user to backend error flow if needed when code is null and estimated outage is not`() {
        viewModel.configurationFinished(false, estimatedOutage = 897)
        viewModel.finishWithError(ErrorType.BACKEND_MAINTENANCE_ERROR)
        viewModel.flowFinished.value?.peekContent()?.let { response ->
            assertThat(response).isInstanceOf(IFaceErrorResponse::class.java)
            assertThat((response as IFaceErrorResponse).reason).isEqualTo(IFaceErrorReason.BACKEND_MAINTENANCE_ERROR)
        }
    }

    @Test
    fun `route user to backend maintenance error flow if needed`() {
        viewModel.configurationFinished(false)
        viewModel.finishWithError(ErrorType.BACKEND_MAINTENANCE_ERROR)
        viewModel.flowFinished.value?.peekContent()?.let { response ->
            assertThat(response).isInstanceOf(IFaceErrorResponse::class.java)
            assertThat((response as IFaceErrorResponse).reason).isEqualTo(IFaceErrorReason.BACKEND_MAINTENANCE_ERROR)
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
            FaceSample(UUID.randomUUID().toString(), ByteArray(0), securedImageRef, IFaceTemplateFormat.RANK_ONE_1_23)
        val result = FaceCaptureResult(0, sample)
        val captureResults = listOf(result)
        return FaceCaptureResponse(captureResults)
    }

    private fun generateFakeMatchResponse(): FaceMatchResponse {
        val captureResults = generateFaceMatchResults(10)
        return FaceMatchResponse(captureResults)
    }

}
