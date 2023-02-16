package com.simprints.eventsystem.event.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent
import org.junit.Test

class ApiAuthenticationPayloadKtTest {

    @Test
    fun `should map backend error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.BACKEND_MAINTENANCE_ERROR.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.BACKEND_MAINTENANCE_ERROR::class.java)
    }

    @Test
    fun `should map offline error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.OFFLINE.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.OFFLINE::class.java)
    }

    @Test
    fun `should map bad creds error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.BAD_CREDENTIALS.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.BAD_CREDENTIALS::class.java)
    }

    @Test
    fun `should map technical error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.TECHNICAL_FAILURE.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.TECHNICAL_FAILURE::class.java)
    }

    @Test
    fun `should map integrity service error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.INTEGRITY_SERVICE_ERROR.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.INTEGRITY_SERVICE_ERROR::class.java)
    }

    @Test
    fun `should map unknown error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.UNKNOWN.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.TECHNICAL_FAILURE::class.java)
    }
}
