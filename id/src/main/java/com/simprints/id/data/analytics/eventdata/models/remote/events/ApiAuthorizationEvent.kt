package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.AuthorizationEvent

@Keep
class ApiAuthorizationEvent(val relativeStartTime: Long,
                            val result: ApiResult,
                            val userInfo: ApiUserInfo?) :ApiEvent(ApiEventType.AUTHORIZATION) {

    @Keep
    class ApiUserInfo(val projectId: String, val userId: String) {

        constructor(userInfoDomain: AuthorizationEvent.UserInfo) :
            this(userInfoDomain.projectId, userInfoDomain.userId)
    }

    @Keep
    enum class ApiResult {
        AUTHORIZED, NOT_AUTHORIZED
    }

    constructor(authorizationEventDomain: AuthorizationEvent) :
        this(authorizationEventDomain.relativeStartTime,
            ApiResult.valueOf(authorizationEventDomain.result.toString()),
            authorizationEventDomain.userInfo?.let { ApiUserInfo(it) })
}
