package com.simprints.infra.authstore.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.authstore.domain.models.AuthRequest
import org.junit.Test

class ApiAuthRequestBodyTest {

    @Test
    fun `should map the model correctly`() {
        val apiAuthRequestBody = ApiAuthRequestBody(
            encryptedProjectSecret = "encryptedProjectSecret",
            integrityToken = "integrityToken",
            deviceId = "deviceId",
        )

        val authRequestBody = AuthRequest(
            encryptedProjectSecret = "encryptedProjectSecret",
            integrityToken = "integrityToken",
            deviceId = "deviceId",
        )
        assertThat(ApiAuthRequestBody.fromDomain(authRequestBody)).isEqualTo(apiAuthRequestBody)
    }
}
