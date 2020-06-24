package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.AuthorizationEvent
import com.simprints.id.data.db.event.domain.events.AuthorizationEvent.AuthorizationPayload.UserInfo
import com.simprints.id.data.db.session.remote.events.ApiEvent

@Keep
class ApiAuthorizationEvent(val relativeStartTime: Long,
                            val result: ApiResult,
                            val userInfo: ApiUserInfo?) : ApiEvent(ApiEventType.AUTHORIZATION) {

    @Keep
    class ApiUserInfo(val projectId: String, val userId: String) {

        constructor(userInfoDomain: UserInfo) :
            this(userInfoDomain.projectId, userInfoDomain.userId)
    }

    @Keep
    enum class ApiResult {
        AUTHORIZED, NOT_AUTHORIZED
    }

    constructor(authorizationEventDomain: AuthorizationEvent) :
        this((authorizationEventDomain.payload as AuthorizationEvent.AuthorizationPayload).relativeStartTime,
            ApiResult.valueOf(authorizationEventDomain.payload.userInfo.toString()),
            authorizationEventDomain.payload.userInfo?.let { ApiUserInfo(it) })
}
