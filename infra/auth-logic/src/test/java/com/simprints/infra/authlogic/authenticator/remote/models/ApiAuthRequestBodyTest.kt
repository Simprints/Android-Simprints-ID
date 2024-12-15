package com.simprints.infra.authlogic.authenticator.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.authstore.domain.models.AuthRequest
import org.junit.Test

class ApiAuthRequestBodyTest {
    @Test
    fun `should map the model correctly`() {
        val apiAuthRequestBody = ApiAuthRequestBody(
            projectSecret = "projectSecret",
            integrityToken = "integrityToken",
        )

        val authRequestBody = AuthRequest(
            projectSecret = "projectSecret",
            integrityToken = "integrityToken",
        )
        assertThat(ApiAuthRequestBody.fromDomain(authRequestBody)).isEqualTo(apiAuthRequestBody)
    }
}
