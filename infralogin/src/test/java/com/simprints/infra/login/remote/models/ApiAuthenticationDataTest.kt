package com.simprints.infra.login.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.login.domain.models.AuthenticationData
import org.junit.Test

class ApiAuthenticationDataTest {

    @Test
    fun `should map the model correctly`() {
        val apiAuthenticationData = ApiAuthenticationData(
            publicKey = "publicKey",
            nonce = "nonce"
        )

        val authenticationData = AuthenticationData(
            publicKey = "publicKey",
            nonce = "nonce"
        )
        assertThat(apiAuthenticationData.toDomain()).isEqualTo(authenticationData)
    }
}
