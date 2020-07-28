package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.*
import com.simprints.id.data.db.event.remote.events.ApiAuthenticationPayload.ApiResult

@Keep
class ApiAuthenticationPayload(createdAt: Long,
                               version: Int,
                               val endedAt: Long,
                               val userInfo: ApiUserInfo,
                               val result: ApiResult) : ApiEventPayload(ApiEventPayloadType.AUTHENTICATION, version, createdAt) {

    @Keep
    class ApiUserInfo(val projectId: String, val userId: String) {
        constructor(userInfoDomain: AuthenticationPayload.UserInfo) :
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

    constructor(domainPayload: AuthenticationPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.endedAt,
            ApiUserInfo(domainPayload.userInfo),
            domainPayload.result.fromDomainToApi())
}


fun AuthenticationPayload.Result.fromDomainToApi() =
    when (this) {
        AUTHENTICATED -> ApiResult.AUTHENTICATED
        BAD_CREDENTIALS -> ApiResult.BAD_CREDENTIALS
        OFFLINE -> ApiResult.OFFLINE
        TECHNICAL_FAILURE -> ApiResult.TECHNICAL_FAILURE
        SAFETYNET_UNAVAILABLE -> ApiResult.SAFETYNET_UNAVAILABLE
        SAFETYNET_INVALID_CLAIM -> ApiResult.SAFETYNET_INVALID_CLAIM
        UNKNOWN -> ApiResult.TECHNICAL_FAILURE
    }
