package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import com.google.common.truth.Truth
import com.simprints.fingerprint.activities.alert.AlertError
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintErrorReason.*
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintErrorReason.Companion.fromFingerprintAlertToErrorResponse
import org.junit.Test


class FingerprintErrorReasonTest {


    @Test
    fun `should map AlertError to FingerprintErrorResponse correctly`() {
        mapOf(
            AlertError.BLUETOOTH_NOT_SUPPORTED to BLUETOOTH_NOT_SUPPORTED,
        AlertError.UNEXPECTED_ERROR to UNEXPECTED_ERROR,
        AlertError.BLUETOOTH_NO_PERMISSION to BLUETOOTH_NO_PERMISSION,

        //User can not leave these alerts, so Fingerprint module should not produce any error response for them.
        AlertError.BLUETOOTH_NOT_ENABLED to UNEXPECTED_ERROR,
        AlertError.NOT_PAIRED to UNEXPECTED_ERROR,
        AlertError.MULTIPLE_PAIRED_SCANNERS to UNEXPECTED_ERROR,
        AlertError.DISCONNECTED to UNEXPECTED_ERROR,
        AlertError.LOW_BATTERY to UNEXPECTED_ERROR
        ).forEach {
            Truth.assertThat(fromFingerprintAlertToErrorResponse(it.key)).isEqualTo(FingerprintErrorResponse(it.value))
        }
    }

}