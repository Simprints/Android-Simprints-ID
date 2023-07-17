package com.simprints.fingerprint.data.domain.moduleapi.fingerprint

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintRefusalFormResponse
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason
import com.simprints.moduleapi.fingerprint.responses.IFingerprintExitReason
import io.mockk.every
import io.mockk.mockk
import org.junit.Test


internal class DomainToFingerprintResponseTest {


    @Test
    fun `maps domain refusal reason to fingerprint form correctly`() {
        mapOf(
            RefusalFormReason.REFUSED_RELIGION to IFingerprintExitReason.REFUSED_RELIGION,
            RefusalFormReason.REFUSED_DATA_CONCERNS to IFingerprintExitReason.REFUSED_DATA_CONCERNS,
            RefusalFormReason.REFUSED_PERMISSION to IFingerprintExitReason.REFUSED_PERMISSION,
            RefusalFormReason.APP_NOT_WORKING to IFingerprintExitReason.APP_NOT_WORKING,
            RefusalFormReason.SCANNER_NOT_WORKING to IFingerprintExitReason.SCANNER_NOT_WORKING,
            RefusalFormReason.REFUSED_NOT_PRESENT to IFingerprintExitReason.REFUSED_NOT_PRESENT,
            RefusalFormReason.REFUSED_YOUNG to IFingerprintExitReason.REFUSED_YOUNG,
            RefusalFormReason.OTHER to IFingerprintExitReason.OTHER
        ).forEach { (formReason, exitReason) ->
            val extraValue = "extraValue"
            val formResponse: FingerprintRefusalFormResponse = mockk {
                every { reason } returns formReason
                every { extra } returns extraValue
            }
            with(DomainToFingerprintResponse.fromDomainToFingerprintRefusalFormResponse(formResponse)) {
                assertThat(reason).isEqualTo(exitReason)
                assertThat(extra).isEqualTo(extraValue)
            }
        }
    }
}