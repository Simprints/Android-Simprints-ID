package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.*
import com.simprints.eventsystem.event.remote.models.ApiAuthenticationPayload.ApiResult

@Keep
data class ApiAuthenticationPayload(override val startTime: Long,
                                    override val version: Int,
                                    val endTime: Long,
                                    val userInfo: ApiUserInfo,
                                    val result: ApiResult) : ApiEventPayload(ApiEventPayloadType.Authentication, version, startTime) {

    @Keep
    data class ApiUserInfo(val projectId: String, val userId: String) {
        constructor(userInfoDomain: AuthenticationPayload.UserInfo) :
            this(userInfoDomain.projectId, userInfoDomain.userId)
    }

    @Keep
    enum class ApiResult {
        AUTHENTICATED,
        BAD_CREDENTIALS,
        OFFLINE,
        BACKEND_MAINTENANCE_ERROR,
        TECHNICAL_FAILURE,
        PLAY_INTEGRITY_UNAVAILABLE,
    }

    constructor(domainPayload: AuthenticationPayload):
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
        PLAY_INTEGRITY_UNAVAILABLE -> ApiResult.PLAY_INTEGRITY_UNAVAILABLE
        BACKEND_MAINTENANCE_ERROR -> ApiResult.BACKEND_MAINTENANCE_ERROR
        UNKNOWN -> ApiResult.TECHNICAL_FAILURE
    }
