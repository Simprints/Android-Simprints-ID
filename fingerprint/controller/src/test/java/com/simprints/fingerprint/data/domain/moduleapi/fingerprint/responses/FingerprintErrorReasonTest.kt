package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.activities.alert.AlertError
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintErrorReason.Companion.fromFingerprintAlertToErrorResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintErrorReason.UNEXPECTED_ERROR
import org.junit.Test


class FingerprintErrorReasonTest {

    @Test
    fun `should map AlertError to FingerprintErrorResponse correctly`() {
        assertThat(fromFingerprintAlertToErrorResponse(AlertError.UNEXPECTED_ERROR))
            .isEqualTo(FingerprintErrorResponse(UNEXPECTED_ERROR))
    }
}
