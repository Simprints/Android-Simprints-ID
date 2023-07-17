package com.simprints.id.domain.moduleapi.face.responses

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormReason
import com.simprints.moduleapi.face.responses.IFaceExitFormResponse
import com.simprints.moduleapi.face.responses.IFaceExitReason
import io.mockk.every
import io.mockk.mockk
import org.junit.Test


internal class FaceExitReasonTest {

    @Test
    fun `maps face refusal option to refusal form reason correctly`() {
        mapOf(
            FaceExitReason.REFUSED_RELIGION to RefusalFormReason.REFUSED_RELIGION,
            FaceExitReason.REFUSED_DATA_CONCERNS to RefusalFormReason.REFUSED_DATA_CONCERNS,
            FaceExitReason.REFUSED_PERMISSION to RefusalFormReason.REFUSED_PERMISSION,
            FaceExitReason.APP_NOT_WORKING to RefusalFormReason.APP_NOT_WORKING,
            FaceExitReason.SCANNER_NOT_WORKING to RefusalFormReason.SCANNER_NOT_WORKING,
            FaceExitReason.REFUSED_NOT_PRESENT to RefusalFormReason.REFUSED_NOT_PRESENT,
            FaceExitReason.REFUSED_YOUNG to RefusalFormReason.REFUSED_YOUNG,
            FaceExitReason.OTHER to RefusalFormReason.OTHER
        ).forEach { (option, reason) ->
            assertThat(option.toAppRefusalFormReason()).isEqualTo(reason)
        }
    }

    @Test
    fun `maps module api refusal option to domain correctly`() {
        mapOf(
            IFaceExitReason.REFUSED_RELIGION to FaceExitReason.REFUSED_RELIGION,
            IFaceExitReason.REFUSED_DATA_CONCERNS to FaceExitReason.REFUSED_DATA_CONCERNS,
            IFaceExitReason.REFUSED_PERMISSION to FaceExitReason.REFUSED_PERMISSION,
            IFaceExitReason.APP_NOT_WORKING to FaceExitReason.APP_NOT_WORKING,
            IFaceExitReason.SCANNER_NOT_WORKING to FaceExitReason.SCANNER_NOT_WORKING,
            IFaceExitReason.REFUSED_NOT_PRESENT to FaceExitReason.REFUSED_NOT_PRESENT,
            IFaceExitReason.REFUSED_YOUNG to FaceExitReason.REFUSED_YOUNG,
            IFaceExitReason.OTHER to FaceExitReason.OTHER
        ).forEach { (formReason, exitReason) ->
            val extraValue = "extraValue"
            val formResponse: IFaceExitFormResponse = mockk {
                every { reason } returns formReason
                every { extra } returns extraValue
            }
            with(formResponse.fromModuleApiToDomain()) {
                assertThat(reason).isEqualTo(exitReason)
                assertThat(extra).isEqualTo(extraValue)
            }
        }
    }
}