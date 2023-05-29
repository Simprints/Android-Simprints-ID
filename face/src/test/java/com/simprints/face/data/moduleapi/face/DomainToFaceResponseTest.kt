package com.simprints.face.data.moduleapi.face

import com.google.common.truth.Truth.assertThat
import com.simprints.face.controllers.core.events.model.RefusalAnswer
import com.simprints.face.data.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.face.data.moduleapi.face.responses.FaceConfigurationResponse
import com.simprints.face.data.moduleapi.face.responses.FaceErrorReason
import com.simprints.face.data.moduleapi.face.responses.FaceErrorResponse
import com.simprints.face.data.moduleapi.face.responses.FaceExitFormResponse
import com.simprints.face.data.moduleapi.face.responses.FaceMatchResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.face.data.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.face.data.moduleapi.face.responses.entities.FaceSample
import com.simprints.face.data.moduleapi.face.responses.entities.Path
import com.simprints.face.data.moduleapi.face.responses.entities.SecuredImageRef
import com.simprints.infra.events.sampledata.FACE_TEMPLATE_FORMAT
import com.simprints.moduleapi.face.responses.IFaceCaptureResponse
import com.simprints.moduleapi.face.responses.IFaceConfigurationResponse
import com.simprints.moduleapi.face.responses.IFaceErrorReason
import com.simprints.moduleapi.face.responses.IFaceErrorResponse
import com.simprints.moduleapi.face.responses.IFaceExitFormResponse
import com.simprints.moduleapi.face.responses.IFaceExitReason
import com.simprints.moduleapi.face.responses.IFaceMatchResponse
import com.simprints.moduleapi.face.responses.IFaceResponse
import org.junit.Test
import java.util.UUID

class DomainToFaceResponseTest {

    private val path = Path(arrayOf("file://images/someFile"))

    @Test
    fun `Map to capture response`() {
        val captureResults = listOf(generateCaptureResult())
        val response = FaceCaptureResponse(captureResults)

        val iFaceResponse: IFaceResponse = DomainToFaceResponse.fromDomainToFaceResponse(response)

        assertThat(iFaceResponse).isInstanceOf(IFaceCaptureResponse::class.java)

        with(iFaceResponse as IFaceCaptureResponse) {
            assertThat(capturingResult.size).isEqualTo(1)

            val first = capturingResult.first()
            assertThat(first.index).isEqualTo(0)

            val sample = first.sample
            assertThat(sample?.imageRef?.path).isEqualTo(path)

            assertThat(sample?.format).isEqualTo(FACE_TEMPLATE_FORMAT)
        }
    }

    @Test
    fun `Map to match response`() {
        val matchResults = listOf(generateMatchResult())
        val response = FaceMatchResponse(matchResults)

        val iFaceResponse: IFaceResponse = DomainToFaceResponse.fromDomainToFaceResponse(response)

        assertThat(iFaceResponse).isInstanceOf(IFaceMatchResponse::class.java)

        with(iFaceResponse as IFaceMatchResponse) {
            assertThat(result.size).isEqualTo(1)

            val first = result.first()
            assertThat(first.guid).isEqualTo(matchResults.first().guid)
            assertThat(first.confidence).isEqualTo(matchResults.first().confidence)
        }
    }

    @Test
    fun `Map to error response`() {
        var response = FaceErrorResponse(FaceErrorReason.LICENSE_MISSING)
        var iFaceResponse: IFaceResponse = DomainToFaceResponse.fromDomainToFaceResponse(response)
        assertThat((iFaceResponse as IFaceErrorResponse).reason).isEqualTo(IFaceErrorReason.LICENSE_MISSING)

        response = FaceErrorResponse(FaceErrorReason.LICENSE_INVALID)
        iFaceResponse = DomainToFaceResponse.fromDomainToFaceResponse(response)
        assertThat((iFaceResponse as IFaceErrorResponse).reason).isEqualTo(IFaceErrorReason.LICENSE_INVALID)

        response = FaceErrorResponse(FaceErrorReason.UNEXPECTED_ERROR)
        iFaceResponse = DomainToFaceResponse.fromDomainToFaceResponse(response)
        assertThat((iFaceResponse as IFaceErrorResponse).reason).isEqualTo(IFaceErrorReason.UNEXPECTED_ERROR)

        response = FaceErrorResponse(FaceErrorReason.CONFIGURATION_ERROR)
        iFaceResponse = DomainToFaceResponse.fromDomainToFaceResponse(response)
        assertThat((iFaceResponse as IFaceErrorResponse).reason).isEqualTo(IFaceErrorReason.FACE_CONFIGURATION_ERROR)
    }

    @Test
    fun `Map to exit form response`() {
        var response = FaceExitFormResponse(RefusalAnswer.REFUSED_RELIGION, "Some extra")
        var iFaceResponse: IFaceResponse = DomainToFaceResponse.fromDomainToFaceResponse(response)
        with(iFaceResponse as IFaceExitFormResponse) {
            assertThat(extra).isEqualTo("Some extra")
            assertThat(reason).isEqualTo(IFaceExitReason.REFUSED_RELIGION)
        }

        response = FaceExitFormResponse(RefusalAnswer.REFUSED_DATA_CONCERNS, "Some extra")
        iFaceResponse = DomainToFaceResponse.fromDomainToFaceResponse(response)
        with(iFaceResponse as IFaceExitFormResponse) {
            assertThat(extra).isEqualTo("Some extra")
            assertThat(reason).isEqualTo(IFaceExitReason.REFUSED_DATA_CONCERNS)
        }

        response = FaceExitFormResponse(RefusalAnswer.REFUSED_PERMISSION, "Some extra")
        iFaceResponse = DomainToFaceResponse.fromDomainToFaceResponse(response)
        with(iFaceResponse as IFaceExitFormResponse) {
            assertThat(extra).isEqualTo("Some extra")
            assertThat(reason).isEqualTo(IFaceExitReason.REFUSED_PERMISSION)
        }

        response = FaceExitFormResponse(RefusalAnswer.APP_NOT_WORKING, "Some extra")
        iFaceResponse = DomainToFaceResponse.fromDomainToFaceResponse(response)
        with(iFaceResponse as IFaceExitFormResponse) {
            assertThat(extra).isEqualTo("Some extra")
            assertThat(reason).isEqualTo(IFaceExitReason.APP_NOT_WORKING)
        }

        response = FaceExitFormResponse(RefusalAnswer.REFUSED_NOT_PRESENT, "Some extra")
        iFaceResponse = DomainToFaceResponse.fromDomainToFaceResponse(response)
        with(iFaceResponse as IFaceExitFormResponse) {
            assertThat(extra).isEqualTo("Some extra")
            assertThat(reason).isEqualTo(IFaceExitReason.REFUSED_NOT_PRESENT)
        }

        response = FaceExitFormResponse(RefusalAnswer.REFUSED_YOUNG, "Some extra")
        iFaceResponse = DomainToFaceResponse.fromDomainToFaceResponse(response)
        with(iFaceResponse as IFaceExitFormResponse) {
            assertThat(extra).isEqualTo("Some extra")
            assertThat(reason).isEqualTo(IFaceExitReason.REFUSED_YOUNG)
        }

        response = FaceExitFormResponse(RefusalAnswer.OTHER, "Some extra")
        iFaceResponse = DomainToFaceResponse.fromDomainToFaceResponse(response)
        with(iFaceResponse as IFaceExitFormResponse) {
            assertThat(extra).isEqualTo("Some extra")
            assertThat(reason).isEqualTo(IFaceExitReason.OTHER)
        }
    }

    @Test
    fun `Map to configuration response`() {
        val response = FaceConfigurationResponse()

        val iFaceResponse: IFaceResponse = DomainToFaceResponse.fromDomainToFaceResponse(response)

        assertThat(iFaceResponse).isInstanceOf(IFaceConfigurationResponse::class.java)
    }

    private fun generateCaptureResult(): FaceCaptureResult {
        val securedImageRef = SecuredImageRef(path)
        val sample =
            FaceSample(UUID.randomUUID().toString(), ByteArray(0), securedImageRef, FACE_TEMPLATE_FORMAT)
        return FaceCaptureResult(0, sample)
    }

    private fun generateMatchResult(): FaceMatchResult {
        return FaceMatchResult(UUID.randomUUID().toString(), 0.9f)
    }

}
