package com.simprints.id.secure.models

import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result

sealed class AuthenticateDataResult {
    object Authenticated : AuthenticateDataResult()
    object BadCredentials : AuthenticateDataResult()
    object Offline : AuthenticateDataResult()
    object TechnicalFailure : AuthenticateDataResult()
    object SafetyNetUnavailable : AuthenticateDataResult()
    object SafetyNetInvalidClaim : AuthenticateDataResult()
    data class BackendMaintenanceError(val estimatedOutage: Long? = null) : AuthenticateDataResult()
    object Unknown : AuthenticateDataResult()
}

fun AuthenticateDataResult.toDomainResult(): Result =
    when(this){
        AuthenticateDataResult.Authenticated -> Result.AUTHENTICATED
        is AuthenticateDataResult.BackendMaintenanceError -> Result.BACKEND_MAINTENANCE_ERROR
        AuthenticateDataResult.BadCredentials -> Result.BAD_CREDENTIALS
        AuthenticateDataResult.Offline -> Result.OFFLINE
        AuthenticateDataResult.SafetyNetInvalidClaim -> Result.SAFETYNET_INVALID_CLAIM
        AuthenticateDataResult.SafetyNetUnavailable -> Result.SAFETYNET_UNAVAILABLE
        AuthenticateDataResult.TechnicalFailure -> Result.TECHNICAL_FAILURE
        AuthenticateDataResult.Unknown -> Result.UNKNOWN
    }
