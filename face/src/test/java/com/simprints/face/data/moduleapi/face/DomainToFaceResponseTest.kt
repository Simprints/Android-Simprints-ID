package com.simprints.face.data.moduleapi.face

import com.google.common.truth.Truth.assertThat
import com.simprints.face.data.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.face.data.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.face.data.moduleapi.face.responses.entities.FaceSample
import com.simprints.face.data.moduleapi.face.responses.entities.Path
import com.simprints.face.data.moduleapi.face.responses.entities.SecuredImageRef
import com.simprints.moduleapi.face.responses.IFaceCaptureResponse
import com.simprints.moduleapi.face.responses.IFaceResponse
import org.junit.Test
import java.util.*

class DomainToFaceResponseTest {

    private val path = Path(arrayOf("file://images/someFile"))

    @Test
    fun fromDomainToFaceResponse() {
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
        }
    }

    private fun generateCaptureResult(): FaceCaptureResult {
        val securedImageRef = SecuredImageRef(path)
        val sample = FaceSample(UUID.randomUUID().toString(), ByteArray(0), securedImageRef)
        return FaceCaptureResult(0, sample)
    }

}
