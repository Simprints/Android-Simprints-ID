package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.AUTHENTICATED
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.BACKEND_MAINTENANCE_ERROR
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.BAD_CREDENTIALS
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.INTEGRITY_SERVICE_ERROR
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.INTEGRITY_SERVICE_TEMPORARY_DOWN_ERROR
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.MISSING_OR_OUTDATED_PLAY_STORE_ERROR
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.OFFLINE
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.TECHNICAL_FAILURE
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.UNKNOWN
import com.simprints.infra.eventsync.event.remote.models.ApiAuthenticationPayload.ApiResult

@Keep
internal data class ApiAuthenticationPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val userInfo: ApiUserInfo,
    val result: ApiResult,
) : ApiEventPayload(startTime) {
    @Keep
    data class ApiUserInfo(
        val projectId: String,
        val userId: String,
    ) {
        constructor(userInfoDomain: AuthenticationPayload.UserInfo) :
            this(userInfoDomain.projectId, userInfoDomain.userId.value)
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
        MISSING_OR_OUTDATED_PLAY_STORE_ERROR,
    }

    constructor(domainPayload: AuthenticationPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi(),
        ApiUserInfo(domainPayload.userInfo),
        domainPayload.result.fromDomainToApi(),
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = when (tokenKeyType) {
        TokenKeyType.AttendantId -> "userInfo.userId"
        else -> null
    }
}

internal fun AuthenticationPayload.Result.fromDomainToApi() = when (this) {
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
