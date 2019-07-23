package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.AuthenticationEvent

@Keep
class ApiAuthenticationEvent(val relativeStartTime: Long,
                             val relativeEndTime: Long,
                             val userInfo: ApiUserInfo,
                             val result: ApiResult) : ApiEvent(ApiEventType.AUTHENTICATION) {

    @Keep
    class ApiUserInfo(val projectId: String, val userId: String) {
        constructor(userInfoDomain: AuthenticationEvent.UserInfo):
            this(userInfoDomain.projectId, userInfoDomain.userId)
    }

    @Keep
    enum class ApiResult {
        AUTHENTICATED,
        BAD_CREDENTIALS,
        OFFLINE,
        TECHNICAL_FAILURE,
        SAFETYNET_UNAVAILABLE,
        SAFETYNET_INVALID_CLAIM
    }

    constructor(authenticationEventDomain: AuthenticationEvent) :
        this(authenticationEventDomain.relativeStartTime ?: 0,
            authenticationEventDomain.relativeEndTime ?: 0,
            ApiUserInfo(authenticationEventDomain.userInfo),
            authenticationEventDomain.result.toApiAuthenticationEventResult())
}

fun AuthenticationEvent.Result.toApiAuthenticationEventResult() =
    when(this) {
        AuthenticationEvent.Result.AUTHENTICATED -> ApiAuthenticationEvent.ApiResult.AUTHENTICATED
        AuthenticationEvent.Result.BAD_CREDENTIALS -> ApiAuthenticationEvent.ApiResult.BAD_CREDENTIALS
        AuthenticationEvent.Result.OFFLINE -> ApiAuthenticationEvent.ApiResult.OFFLINE
        AuthenticationEvent.Result.TECHNICAL_FAILURE -> ApiAuthenticationEvent.ApiResult.TECHNICAL_FAILURE
        AuthenticationEvent.Result.SAFETYNET_UNAVAILABLE -> ApiAuthenticationEvent.ApiResult.SAFETYNET_UNAVAILABLE
        AuthenticationEvent.Result.SAFETYNET_INVALID_CLAIM -> ApiAuthenticationEvent.ApiResult.SAFETYNET_INVALID_CLAIM
    }
