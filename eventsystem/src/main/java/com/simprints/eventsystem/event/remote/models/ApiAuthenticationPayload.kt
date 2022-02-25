package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.Authenticated
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.BackendMaintenanceError
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.BadCredentials
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.Offline
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.SafetyNetInvalidClaim
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.SafetyNetUnavailable
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.TechnicalFailure
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.Unknown
import com.simprints.eventsystem.event.remote.models.ApiAuthenticationPayload.ApiResult

@Keep
data class ApiAuthenticationPayload(override val startTime: Long,
                                    override val version: Int,
                                    val endTime: Long,
                                    val userInfo: ApiUserInfo,
                                    val result: ApiResult) : ApiEventPayload(ApiEventPayloadType.Authentication, version, startTime) {

    @Keep
    data class ApiUserInfo(val projectId: String, val userId: String) {
        constructor(userInfoDomain: AuthenticationPayload.UserInfo) :
            this(userInfoDomain.projectId, userInfoDomain.userId)
    }

    @Keep
    enum class ApiResult {
        AUTHENTICATED,
        BAD_CREDENTIALS,
        OFFLINE,
        BACKEND_MAINTENANCE_ERROR,
        TECHNICAL_FAILURE,
        SAFETYNET_UNAVAILABLE,
        SAFETYNET_INVALID_CLAIM
    }

    constructor(domainPayload: AuthenticationPayload):
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.endedAt,
            ApiUserInfo(domainPayload.userInfo),
            domainPayload.result.fromDomainToApi())
}


fun AuthenticationPayload.Result.fromDomainToApi() =
    when (this) {
        Authenticated -> ApiResult.AUTHENTICATED
        BadCredentials -> ApiResult.BAD_CREDENTIALS
        Offline -> ApiResult.OFFLINE
        TechnicalFailure -> ApiResult.TECHNICAL_FAILURE
        SafetyNetUnavailable -> ApiResult.SAFETYNET_UNAVAILABLE
        SafetyNetInvalidClaim -> ApiResult.SAFETYNET_INVALID_CLAIM
        is BackendMaintenanceError -> ApiResult.BACKEND_MAINTENANCE_ERROR
        Unknown -> ApiResult.TECHNICAL_FAILURE
    }
