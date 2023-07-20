package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintRefusalFormReason
import com.simprints.moduleapi.fingerprint.responses.IFingerprintExitFormResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintExitReason
import io.mockk.every
import io.mockk.mockk
import org.junit.Test


internal class FingerprintRefusalFormResponseKtTest {

    @Test
    fun `maps module refusal reason to domain correctly`() {
        mapOf(
            IFingerprintExitReason.REFUSED_RELIGION to FingerprintRefusalFormReason.REFUSED_RELIGION,
            IFingerprintExitReason.REFUSED_DATA_CONCERNS to FingerprintRefusalFormReason.REFUSED_DATA_CONCERNS,
            IFingerprintExitReason.REFUSED_PERMISSION to FingerprintRefusalFormReason.REFUSED_PERMISSION,
            IFingerprintExitReason.APP_NOT_WORKING to FingerprintRefusalFormReason.APP_NOT_WORKING,
            IFingerprintExitReason.SCANNER_NOT_WORKING to FingerprintRefusalFormReason.SCANNER_NOT_WORKING,
            IFingerprintExitReason.REFUSED_NOT_PRESENT to FingerprintRefusalFormReason.REFUSED_NOT_PRESENT,
            IFingerprintExitReason.REFUSED_YOUNG to FingerprintRefusalFormReason.REFUSED_YOUNG,
            IFingerprintExitReason.OTHER to FingerprintRefusalFormReason.OTHER
        ).forEach { (formReason, exitReason) ->
            val optionalResponseText = "optionalResponseText"
            val formResponse: IFingerprintExitFormResponse = mockk {
                every { reason } returns formReason
                every { extra } returns optionalResponseText
            }
            with(formResponse.fromModuleApiToDomain()) {
                assertThat(reason).isEqualTo(exitReason)
                assertThat(optionalText).isEqualTo(optionalResponseText)
            }
        }
    }
}