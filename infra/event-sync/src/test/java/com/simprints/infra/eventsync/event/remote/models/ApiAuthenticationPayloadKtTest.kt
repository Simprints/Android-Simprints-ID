package com.simprints.infra.eventsync.event.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.AuthenticationEvent
import io.mockk.mockk
import org.junit.Test

class ApiAuthenticationPayloadKtTest {
    @Test
    fun `should map backend error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.BACKEND_MAINTENANCE_ERROR
                .fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.BACKEND_MAINTENANCE_ERROR::class.java)
    }

    @Test
    fun `should map offline error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.OFFLINE
                .fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.OFFLINE::class.java)
    }

    @Test
    fun `should map bad creds error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.BAD_CREDENTIALS
                .fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.BAD_CREDENTIALS::class.java)
    }

    @Test
    fun `should map technical error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.TECHNICAL_FAILURE
                .fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.TECHNICAL_FAILURE::class.java)
    }

    @Test
    fun `should map outdated play store service error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.MISSING_OR_OUTDATED_PLAY_STORE_ERROR
                .fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.MISSING_OR_OUTDATED_PLAY_STORE_ERROR::class.java)
    }

    @Test
    fun `should map integration service temp down error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.INTEGRITY_SERVICE_TEMPORARY_DOWN_ERROR
                .fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.INTEGRITY_SERVICE_TEMPORARY_DOWN_ERROR::class.java)
    }

    @Test
    fun `should map integrity service error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.INTEGRITY_SERVICE_ERROR
                .fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.INTEGRITY_SERVICE_ERROR::class.java)
    }

    @Test
    fun `should map unknown error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.UNKNOWN
                .fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.TECHNICAL_FAILURE::class.java)
    }

    @Test
    fun `when getTokenizedFieldJsonPath is invoked, correct fields are returned`() {
        val payload = ApiAuthenticationPayload(domainPayload = mockk(relaxed = true))
        TokenKeyType.values().forEach {
            val result = payload.getTokenizedFieldJsonPath(it)
            when (it) {
                TokenKeyType.AttendantId -> assertThat(result).isEqualTo("userInfo.userId")
                else -> assertThat(result).isNull()
            }
        }
    }
}
