package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.*
import com.simprints.infra.eventsync.event.remote.models.ApiAuthenticationPayload.ApiResult

@Keep
internal data class ApiAuthenticationPayload(
    override val startTime: Long,
    override val version: Int,
    val endTime: Long,
    val userInfo: ApiUserInfo,
    val result: ApiResult,
) : ApiEventPayload(ApiEventPayloadType.Authentication, version, startTime) {

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
        INTEGRITY_SERVICE_ERROR,
        INTEGRITY_SERVICE_TEMPORARY_DOWN_ERROR,
        MISSING_OR_OUTDATED_PLAY_STORE_ERROR
    }

    constructor(domainPayload: AuthenticationPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.endedAt,
            ApiUserInfo(domainPayload.userInfo),
            domainPayload.result.fromDomainToApi())
}


internal fun AuthenticationPayload.Result.fromDomainToApi() =
    when (this) {
        AUTHENTICATED -> ApiResult.AUTHENTICATED
        BAD_CREDENTIALS -> ApiResult.BAD_CREDENTIALS
        OFFLINE -> ApiResult.OFFLINE
        TECHNICAL_FAILURE -> ApiResult.TECHNICAL_FAILURE
        INTEGRITY_SERVICE_ERROR -> ApiResult.INTEGRITY_SERVICE_ERROR
        INTEGRITY_SERVICE_TEMPORARY_DOWN_ERROR -> ApiResult.INTEGRITY_SERVICE_TEMPORARY_DOWN_ERROR
        MISSING_OR_OUTDATED_PLAY_STORE_ERROR -> ApiResult.MISSING_OR_OUTDATED_PLAY_STORE_ERROR
        BACKEND_MAINTENANCE_ERROR -> ApiResult.BACKEND_MAINTENANCE_ERROR
        UNKNOWN -> ApiResult.TECHNICAL_FAILURE
    }