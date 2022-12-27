package com.simprints.infra.login.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.login.domain.models.AuthRequest
import org.junit.Test

class ApiAuthRequestBodyTest {

    @Test
    fun `should map the model correctly`() {
        val apiAuthRequestBody = ApiAuthRequestBody(
            encryptedProjectSecret = "encryptedProjectSecret",
            safetyNetAttestationResult = "safetyNetAttestationResult",
            deviceId = "deviceId",
        )

        val authRequestBody = AuthRequest(
            encryptedProjectSecret = "encryptedProjectSecret",
            playIntegrityToken = "safetyNetAttestationResult",
            deviceId = "deviceId",
        )
        assertThat(ApiAuthRequestBody.fromDomain(authRequestBody)).isEqualTo(apiAuthRequestBody)
    }
}
