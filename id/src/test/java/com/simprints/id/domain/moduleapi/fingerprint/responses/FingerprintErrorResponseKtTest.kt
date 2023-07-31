package com.simprints.id.domain.moduleapi.fingerprint.responses

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.moduleapi.fingerprint.responses.IFingerprintErrorReason
import org.junit.Test


class FingerprintErrorResponseKtTest {

    @Test
    fun `IFingerprintErrorReason maps correctly to domain`() {
        mapOf(
            IFingerprintErrorReason.UNEXPECTED_ERROR to FingerprintErrorReason.UNEXPECTED_ERROR,
            IFingerprintErrorReason.BLUETOOTH_NOT_SUPPORTED to FingerprintErrorReason.BLUETOOTH_NOT_SUPPORTED,
            IFingerprintErrorReason.FINGERPRINT_CONFIGURATION_ERROR to FingerprintErrorReason.FINGERPRINT_CONFIGURATION_ERROR,
            IFingerprintErrorReason.BLUETOOTH_NO_PERMISSION to FingerprintErrorReason.BLUETOOTH_NO_PERMISSION
        ).forEach {
            assertThat(it.key.fromModuleApiToDomain()).isEqualTo(it.value)
        }
    }

    @Test
    fun `FingerprintErrorReason maps correctly to app error reason`() {
        mapOf(
            FingerprintErrorReason.UNEXPECTED_ERROR to AppErrorResponse.Reason.UNEXPECTED_ERROR,
            FingerprintErrorReason.BLUETOOTH_NOT_SUPPORTED to AppErrorResponse.Reason.BLUETOOTH_NOT_SUPPORTED,
            FingerprintErrorReason.FINGERPRINT_CONFIGURATION_ERROR to AppErrorResponse.Reason.FINGERPRINT_CONFIGURATION_ERROR,
            FingerprintErrorReason.BLUETOOTH_NO_PERMISSION to AppErrorResponse.Reason.BLUETOOTH_NO_PERMISSION
        ).forEach {
            assertThat(it.key.toAppErrorReason()).isEqualTo(it.value)
        }
    }

}