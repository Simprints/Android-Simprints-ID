package com.simprints.id.secure.models

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent
import org.junit.Test

class AuthenticateDataResultKtTest {

    @Test
    fun mapAuthenticatedResult() {
        val result = AuthenticateDataResult.Authenticated.toDomainResult()

        assertThat(result).isInstanceOf(AuthenticationEvent.AuthenticationPayload.Result.AUTHENTICATED::class.java)
    }

    @Test
    fun mapBadCredentialResult() {
        val result = AuthenticateDataResult.BadCredentials.toDomainResult()

        assertThat(result).isInstanceOf(AuthenticationEvent.AuthenticationPayload.Result.BAD_CREDENTIALS::class.java)
    }

    @Test
    fun mapOfflineResult() {
        val result = AuthenticateDataResult.Offline.toDomainResult()

        assertThat(result).isInstanceOf(AuthenticationEvent.AuthenticationPayload.Result.OFFLINE::class.java)
    }

    @Test
    fun mapTechnicalFailureResult() {
        val result = AuthenticateDataResult.TechnicalFailure.toDomainResult()

        assertThat(result).isInstanceOf(AuthenticationEvent.AuthenticationPayload.Result.TECHNICAL_FAILURE::class.java)
    }

    @Test
    fun mapSafetyNetUnavailableResult() {
        val result = AuthenticateDataResult.SafetyNetUnavailable.toDomainResult()

        assertThat(result).isInstanceOf(AuthenticationEvent.AuthenticationPayload.Result.SAFETYNET_UNAVAILABLE::class.java)
    }

    @Test
    fun mapSafetyNetInvalidClaimResult() {
        val result = AuthenticateDataResult.SafetyNetInvalidClaim.toDomainResult()

        assertThat(result).isInstanceOf(AuthenticationEvent.AuthenticationPayload.Result.SAFETYNET_INVALID_CLAIM::class.java)
    }

    @Test
    fun mapBackendMaintenanceErrorResult() {
        val result = AuthenticateDataResult.BackendMaintenanceError().toDomainResult()

        assertThat(result).isInstanceOf(AuthenticationEvent.AuthenticationPayload.Result.BACKEND_MAINTENANCE_ERROR::class.java)
    }

    @Test
    fun mapUnknownResult() {
        val result = AuthenticateDataResult.Unknown.toDomainResult()

        assertThat(result).isInstanceOf(AuthenticationEvent.AuthenticationPayload.Result.UNKNOWN::class.java)
    }
}
