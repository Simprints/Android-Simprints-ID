package com.simprints.infra.authlogic.model

import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result

sealed class AuthenticateDataResult {
    object Authenticated : AuthenticateDataResult()

    object BadCredentials : AuthenticateDataResult()

    object Offline : AuthenticateDataResult()

    object TechnicalFailure : AuthenticateDataResult()

    object IntegrityException : AuthenticateDataResult()

    object IntegrityServiceTemporaryDown : AuthenticateDataResult()

    object MissingOrOutdatedGooglePlayStoreApp : AuthenticateDataResult()

    data class BackendMaintenanceError(
        val estimatedOutage: Long? = null,
    ) : AuthenticateDataResult()

    object Unknown : AuthenticateDataResult()
}

fun AuthenticateDataResult.toDomainResult(): Result = when (this) {
    AuthenticateDataResult.Authenticated -> Result.AUTHENTICATED
    is AuthenticateDataResult.BackendMaintenanceError -> Result.BACKEND_MAINTENANCE_ERROR
    AuthenticateDataResult.BadCredentials -> Result.BAD_CREDENTIALS
    AuthenticateDataResult.Offline -> Result.OFFLINE
    AuthenticateDataResult.IntegrityException -> Result.INTEGRITY_SERVICE_ERROR
    AuthenticateDataResult.IntegrityServiceTemporaryDown -> Result.INTEGRITY_SERVICE_TEMPORARY_DOWN_ERROR
    AuthenticateDataResult.MissingOrOutdatedGooglePlayStoreApp -> Result.MISSING_OR_OUTDATED_PLAY_STORE_ERROR
    AuthenticateDataResult.TechnicalFailure -> Result.TECHNICAL_FAILURE
    AuthenticateDataResult.Unknown -> Result.UNKNOWN
}
