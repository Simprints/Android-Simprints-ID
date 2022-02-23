package com.simprints.id.domain.moduleapi.face.responses

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.moduleapi.face.responses.IFaceErrorReason
import org.junit.Test

class FaceErrorResponseTest {

    @Test
    fun `given backend maintenance error should be mapped appropriately`() {
        val reason = IFaceErrorReason.BACKEND_MAINTENANCE_ERROR

        assertThat(reason.fromModuleApiToDomain()).isInstanceOf(FaceErrorReason.BACKEND_MAINTENANCE_ERROR::class.java)
    }

    @Test
    fun `given license missing error should be mapped appropriately`() {
        val reason = IFaceErrorReason.LICENSE_MISSING

        assertThat(reason.fromModuleApiToDomain()).isInstanceOf(FaceErrorReason.FACE_LICENSE_MISSING::class.java)
    }

    @Test
    fun `given license invalid error should be mapped appropriately`() {
        val reason = IFaceErrorReason.LICENSE_INVALID

        assertThat(reason.fromModuleApiToDomain()).isInstanceOf(FaceErrorReason.FACE_LICENSE_INVALID::class.java)
    }

    @Test
    fun `given backend maintenance error FaceErrorReason should map correctly`() {
        val faceErrorReason = FaceErrorReason.BACKEND_MAINTENANCE_ERROR

        assertThat(faceErrorReason.toAppErrorReason()).isInstanceOf(AppErrorResponse.Reason.BACKEND_MAINTENANCE_ERROR::class.java)
    }
}
