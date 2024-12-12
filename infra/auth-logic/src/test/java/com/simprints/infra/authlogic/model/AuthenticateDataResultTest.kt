package com.simprints.infra.authlogic.model

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.AuthenticationEvent
import org.junit.Test

class AuthenticateDataResultTest {
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
    fun mapMissingOrOutdatedGooglePlayStoreAppErrorResult() {
        val result = AuthenticateDataResult.MissingOrOutdatedGooglePlayStoreApp.toDomainResult()

        assertThat(result).isInstanceOf(AuthenticationEvent.AuthenticationPayload.Result.MISSING_OR_OUTDATED_PLAY_STORE_ERROR::class.java)
    }

    @Test
    fun mapIntegrityServiceTemporaryDownErrorResult() {
        val result = AuthenticateDataResult.IntegrityServiceTemporaryDown.toDomainResult()

        assertThat(result).isInstanceOf(AuthenticationEvent.AuthenticationPayload.Result.INTEGRITY_SERVICE_TEMPORARY_DOWN_ERROR::class.java)
    }

    @Test
    fun mapIntegrityErrorResult() {
        val result = AuthenticateDataResult.IntegrityException.toDomainResult()

        assertThat(result).isInstanceOf(AuthenticationEvent.AuthenticationPayload.Result.INTEGRITY_SERVICE_ERROR::class.java)
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
