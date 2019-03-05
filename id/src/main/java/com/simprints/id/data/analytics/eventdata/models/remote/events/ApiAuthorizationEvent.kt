package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.AuthorizationEvent

class ApiAuthorizationEvent(val relativeStartTime: Long,
                            val result: ApiResult,
                            val userInfo: ApiUserInfo?) :ApiEvent(ApiEventType.AUTHORIZATION) {

    class ApiUserInfo(val projectId: String, val userId: String) {

        constructor(userInfoDomain: AuthorizationEvent.UserInfo) :
            this(userInfoDomain.projectId, userInfoDomain.userId)
    }

    enum class ApiResult {
        AUTHORIZED, NOT_AUTHORIZED
    }

    constructor(authorizationEventDomain: AuthorizationEvent) :
        this(authorizationEventDomain.relativeStartTime,
            ApiResult.valueOf(authorizationEventDomain.result.toString()),
            authorizationEventDomain.userInfo?.let { ApiUserInfo(it) })
}
