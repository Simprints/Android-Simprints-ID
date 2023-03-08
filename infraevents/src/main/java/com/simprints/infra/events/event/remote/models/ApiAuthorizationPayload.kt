package com.simprints.infra.events.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult.AUTHORIZED
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult.NOT_AUTHORIZED
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.UserInfo
import com.simprints.infra.events.remote.models.ApiAuthorizationPayload.ApiResult


@Keep
@JsonInclude(Include.NON_NULL)
data class ApiAuthorizationPayload(override val startTime: Long,
                                   override val version: Int,
                                   val result: ApiResult,
                                   val userInfo: ApiUserInfo?) : ApiEventPayload(ApiEventPayloadType.Authorization, version, startTime) {

    @Keep
    data class ApiUserInfo(val projectId: String, val userId: String) {

        constructor(userInfoDomain: UserInfo) :
            this(userInfoDomain.projectId, userInfoDomain.userId)
    }

    @Keep
    enum class ApiResult {
        AUTHORIZED, NOT_AUTHORIZED
    }

    constructor(domainPayload: AuthorizationPayload):
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.result.fromDomainToApi(),
            domainPayload.userInfo?.let { ApiUserInfo(it) })
}


fun AuthorizationPayload.AuthorizationResult.fromDomainToApi() =
    when (this) {
        AUTHORIZED -> ApiResult.AUTHORIZED
        NOT_AUTHORIZED -> ApiResult.NOT_AUTHORIZED
    }
