package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.AuthorizationEvent
import com.simprints.id.data.db.event.domain.events.AuthorizationEvent.AuthorizationPayload
import com.simprints.id.data.db.event.domain.events.AuthorizationEvent.AuthorizationPayload.UserInfo

@Keep
class ApiAuthorizationEvent(domainEvent: AuthorizationEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {


    @Keep
    class ApiAuthorizationPayload(val relativeStartTime: Long,
                                  val result: ApiResult,
                                  val userInfo: ApiUserInfo?) : ApiEventPayload(ApiEventPayloadType.AUTHORIZATION) {

        @Keep
        class ApiUserInfo(val projectId: String, val userId: String) {

            constructor(userInfoDomain: UserInfo) :
                this(userInfoDomain.projectId, userInfoDomain.userId)
        }

        @Keep
        enum class ApiResult {
            AUTHORIZED, NOT_AUTHORIZED
        }

        constructor(domainPayload: AuthorizationPayload) :
            this(domainPayload.creationTime,
                ApiResult.valueOf(domainPayload.userInfo.toString()),
                domainPayload.userInfo?.let { ApiUserInfo(it) })
    }
}
