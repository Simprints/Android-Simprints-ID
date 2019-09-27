package com.simprints.face.data.moduleapi.face

import com.google.common.truth.Truth.assertThat
import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import com.simprints.moduleapi.face.requests.IFaceRequestType
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class FaceToDomainRequestTest {

    @Test
    fun fromFaceToDomainRequest() {
        val request = mockk<IFaceCaptureRequest> {
            every { nFaceSamplesToCapture } returns 1
            every { type } returns IFaceRequestType.CAPTURE
        }

        val faceRequest = FaceToDomainRequest.fromFaceToDomainRequest(request)
        assertThat(faceRequest).isInstanceOf(FaceCaptureRequest::class.java)
    }
}
