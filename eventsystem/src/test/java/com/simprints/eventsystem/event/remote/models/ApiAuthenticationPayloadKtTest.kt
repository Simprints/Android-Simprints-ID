package com.simprints.eventsystem.event.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent
import org.junit.Test

class ApiAuthenticationPayloadKtTest {

    @Test
    fun `should map backend error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.BACKEND_MAINTENANCE_ERROR().fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.BACKEND_MAINTENANCE_ERROR::class.java)
    }

    @Test
    fun `should map offline error correctly`() {
        val result =
            AuthenticationEvent.AuthenticationPayload.Result.OFFLINE.fromDomainToApi()
        assertThat(result).isInstanceOf(ApiAuthenticationPayload.ApiResult.OFFLINE::class.java)
    }
}
