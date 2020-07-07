package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.AuthenticationEvent
import com.simprints.id.data.db.event.domain.events.AuthenticationEvent.AuthenticationPayload
import com.simprints.id.data.db.event.domain.events.AuthenticationEvent.AuthenticationPayload.Result.*
@Keep
class ApiAuthenticationEvent(val relativeStartTime: Long,
                             val relativeEndTime: Long,
                             val userInfo: ApiUserInfo,
                             val result: ApiResult) : ApiEvent(ApiEventType.AUTHENTICATION) {

    @Keep
    class ApiUserInfo(val projectId: String, val userId: String) {
        constructor(userInfoDomain: AuthenticationPayload.UserInfo):
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
        this((authenticationEventDomain.payload as AuthenticationPayload).creationTime,
            authenticationEventDomain.payload.endTime,
            ApiUserInfo(authenticationEventDomain.payload.userInfo),
            authenticationEventDomain.payload.result.toApiAuthenticationEventResult())
}

fun AuthenticationEvent.AuthenticationPayload.Result.toApiAuthenticationEventResult() =
    when(this) {
        AUTHENTICATED -> ApiAuthenticationEvent.ApiResult.AUTHENTICATED
        BAD_CREDENTIALS -> ApiAuthenticationEvent.ApiResult.BAD_CREDENTIALS
        OFFLINE -> ApiAuthenticationEvent.ApiResult.OFFLINE
        TECHNICAL_FAILURE -> ApiAuthenticationEvent.ApiResult.TECHNICAL_FAILURE
        SAFETYNET_UNAVAILABLE -> ApiAuthenticationEvent.ApiResult.SAFETYNET_UNAVAILABLE
        SAFETYNET_INVALID_CLAIM -> ApiAuthenticationEvent.ApiResult.SAFETYNET_INVALID_CLAIM
        UNKNOWN -> ApiAuthenticationEvent.ApiResult.TECHNICAL_FAILURE
    }
