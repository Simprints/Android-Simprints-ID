package com.simprints.face.data.moduleapi.face

import com.google.common.truth.Truth.assertThat
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import com.simprints.moduleapi.face.requests.IFaceConfigurationRequest
import com.simprints.moduleapi.face.requests.IFaceMatchRequest
import com.simprints.moduleapi.face.requests.IFaceRequest
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class FaceToDomainRequestTest {

    @Test
    fun `Map capture request`() {
        val request = mockk<IFaceCaptureRequest> {
            every { nFaceSamplesToCapture } returns 1
        }

        val faceRequest = FaceToDomainRequest.fromFaceToDomainRequest(request)
        assertThat(faceRequest).isInstanceOf(FaceCaptureRequest::class.java)
    }

    @Test
    fun `Map match request`() {
        val request = mockk<IFaceMatchRequest>(relaxed = true)

        val faceRequest = FaceToDomainRequest.fromFaceToDomainRequest(request)
        assertThat(faceRequest).isInstanceOf(FaceMatchRequest::class.java)
    }

    @Test(expected = InvalidFaceRequestException::class)
    fun `Return exception if unusual request`() {
        val request = mockk<IFaceRequest>(relaxed = true)

        FaceToDomainRequest.fromFaceToDomainRequest(request)
    }
}
