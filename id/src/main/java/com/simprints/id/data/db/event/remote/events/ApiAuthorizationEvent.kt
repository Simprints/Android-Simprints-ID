package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult.AUTHORIZED
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult.NOT_AUTHORIZED
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload.UserInfo
import com.simprints.id.data.db.event.remote.events.ApiAuthorizationEvent.ApiAuthorizationPayload.ApiResult

@Keep
class ApiAuthorizationEvent(domainEvent: AuthorizationEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.map { it.fromDomainToApi() }.toMap(),
        domainEvent.payload.fromDomainToApi()) {


    @Keep
    class ApiAuthorizationPayload(createdAt: Long,
                                  eventVersion: Int,
                                  val result: ApiResult,
                                  val userInfo: ApiUserInfo?) : ApiEventPayload(ApiEventPayloadType.AUTHORIZATION, eventVersion, createdAt) {

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
            this(domainPayload.createdAt,
                domainPayload.eventVersion,
                domainPayload.result.fromDomainToApi(),
                domainPayload.userInfo?.let { ApiUserInfo(it) })
    }
}

fun AuthorizationPayload.AuthorizationResult.fromDomainToApi() =
    when (this) {
        AUTHORIZED -> ApiResult.AUTHORIZED
        NOT_AUTHORIZED -> ApiResult.NOT_AUTHORIZED
    }
