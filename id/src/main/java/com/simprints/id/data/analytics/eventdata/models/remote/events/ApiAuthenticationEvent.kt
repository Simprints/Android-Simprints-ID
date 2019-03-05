package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.AuthenticationEvent

class ApiAuthenticationEvent(val relativeStartTime: Long,
                             val relativeEndTime: Long,
                             val userInfo: ApiUserInfo,
                             val result: ApiResult) : ApiEvent(ApiEventType.AUTHENTICATION) {

    class ApiUserInfo(val projectId: String, val userId: String) {
        constructor(userInfoDomain: AuthenticationEvent.UserInfo):
            this(userInfoDomain.projectId, userInfoDomain.userId)
    }

    enum class ApiResult {
        AUTHENTICATED,
        BAD_CREDENTIALS,
        OFFLINE,
        TECHNICAL_FAILURE
    }

    constructor(authenticationEventDomain: AuthenticationEvent) :
        this(authenticationEventDomain.relativeStartTime,
            authenticationEventDomain.relativeEndTime,
            ApiUserInfo(authenticationEventDomain.userInfo),
            ApiResult.valueOf(authenticationEventDomain.result.toString()))
}
